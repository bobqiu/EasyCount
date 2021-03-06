package com.tencent.easycount.exec.physical;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tencent.easycount.conf.ECConfiguration;
import com.tencent.easycount.exec.io.Data1Source;
import com.tencent.easycount.exec.io.DataIOUtils;
import com.tencent.easycount.exec.io.inner.Data1SourceInner;
import com.tencent.easycount.exec.io.kafka.Data1SourceKafka;
import com.tencent.easycount.exec.io.kafka.KafkaECConsumer;
import com.tencent.easycount.exec.io.local.LocalModeUtils;
import com.tencent.easycount.exec.physical.Task.SOCallBack;
import com.tencent.easycount.exec.physical.Task.SrcObject;
import com.tencent.easycount.exec.physical.Task.TupleProcessor;
import com.tencent.easycount.metastore.Table.TableType;
import com.tencent.easycount.metastore.TableUtils;
import com.tencent.easycount.plan.logical.OpDesc;
import com.tencent.easycount.plan.logical.OpDesc1TS;
import com.tencent.easycount.util.status.StatusPrintable;
import com.tencent.easycount.util.status.TDBankUtils;

public class Data1Generator implements Closeable, StatusPrintable {
	private static Logger log = LoggerFactory.getLogger(Data1Generator.class);
	final private ConcurrentHashMap<String, KafkaECConsumer> tubeConsumers;
	// final private ArrayList<Operator1TS> tsOps;
	final private ArrayList<Data1Source> opsources;
	final private HashMap<String, ObjectInspector> tagKey2OIs;

	// final private SynchronousQueue<SourceObject> objQueue;

	final private String taskName;
	final private String taskId;
	final private String execId;

	final TupleProcessor tupleProcessor;

	@Override
	public void printStatus(final int printId) {
		for (final Data1Source opsrc : this.opsources) {
			opsrc.printStatus(printId);
		}
	}

	public Data1Generator(final ECConfiguration hconf,
			final ArrayList<OpDesc> tsOpDescs, final String taskName,
			final String taskId, final String execId,
			final TupleProcessor tupleProcessor) {
		// this.hconf = hconf;
		this.taskName = taskName;
		this.taskId = taskId;
		this.execId = execId;
		// this.objQueue = new SynchronousQueue<SourceObject>();
		// this.tsOps = tsOps;
		this.opsources = new ArrayList<Data1Source>();
		this.tagKey2OIs = new HashMap<String, ObjectInspector>();
		this.tubeConsumers = new ConcurrentHashMap<String, KafkaECConsumer>();
		this.tupleProcessor = tupleProcessor;

		final boolean localmode = hconf.getBoolean("localmode", false);

		for (int i = 0; i < tsOpDescs.size(); i++) {
			final OpDesc1TS opDesc = (OpDesc1TS) tsOpDescs.get(i);

			Data1Source data1Source = null;
			if (localmode
					&& (opDesc.getTable().getTableType() != TableType.inner)) {
				data1Source = LocalModeUtils.generateLocalDataSource(
						opDesc.getTaskId_OpTagIdx(), opDesc, this, hconf);
			} else {
				if (opDesc.isDimensionTable()) {
					data1Source = DataIOUtils.generateDimDataSource(
							opDesc.getTaskId_OpTagIdx(), opDesc, this);
					// } else if (hconf.getBoolean("testmode", false)) {
					// data1Source = new
					// Data1SourceTest(opDesc.getTaskId_OpTagIdx(),
					// opDesc, this, hconf);
				} else if (opDesc.getTable().getTableType() == TableType.inner) {
					data1Source = new Data1SourceInner(
							opDesc.getTaskId_OpTagIdx(), opDesc, this, hconf);
				} else if (opDesc.getTable().getTableType() == TableType.kafka) {

					final String tubeMaster = TableUtils
							.getTableTubeMaster(opDesc.getTable());
					final int tubePort = TableUtils.getTableTubePort(opDesc
							.getTable());
					final String tubeAddrList = TableUtils
							.getTableTubeAddrList(opDesc.getTable());

					final String topic = TableUtils.getTableTopic(opDesc
							.getTable());

					final String zkid = tubeMaster + "-" + tubePort + "-"
							+ topic;
					if (!this.tubeConsumers.containsKey(zkid)) {
						this.tubeConsumers.put(zkid, new KafkaECConsumer(hconf,
								tubeMaster, tubePort, tubeAddrList, topic,
								taskId, execId));
						log.info("create a new consumer : " + zkid);
					}

					data1Source = new Data1SourceKafka(
							opDesc.getTaskId_OpTagIdx(), opDesc, this, hconf,
							this.tubeConsumers.get(zkid));
				}
			}

			this.opsources.add(data1Source);
			this.tagKey2OIs.put(opDesc.getTaskId_OpTagIdx(),
					data1Source.getObjectInspector());

			log.info("generate datasource : " + i + " : "
					+ opDesc.getTable().toString());
		}
	}

	public synchronized void start() {
		for (final Data1Source opsrc : this.opsources) {
			opsrc.start();
		}
	}

	/**
	 * there may be multi thread emit obj, but only one(may be) thread process
	 * them
	 *
	 * @param attrs
	 */
	public boolean emit(final String tagKey, final Object obj,
			final SOCallBack socb) {
		final SrcObject so = new SrcObject(tagKey, obj, socb);
		try {
			int xxx = 0;
			while (!this.tupleProcessor.offer(so)) {
				log.warn(this.taskName + " : " + this.taskId + "-"
						+ this.execId + " : srcObject offer fail : "
						+ so.toString() + " for " + (xxx++) + " times");
			}
			// return after the obj been processed
			so.await();
		} catch (final Throwable e) {
			log.error(TDBankUtils.getExceptionStack(e));
			return false;
		}
		return true;
	}

	@Override
	public void close() {
		log.info("begin to stop datagenerator : " + this.taskName + " : "
				+ this.taskId + "-" + this.execId);
		for (final Data1Source opsrc : this.opsources) {
			try {
				opsrc.close();
			} catch (final IOException e) {
				log.error(TDBankUtils.getExceptionStack(e));
			}
		}
		log.info("datagenerator stopped : " + this.taskName + " : "
				+ this.taskId);

	}

	public void restart() {
		for (final Data1Source opsrc : this.opsources) {
			try {
				opsrc.restart();
			} catch (final Exception e) {
				log.error(TDBankUtils.getExceptionStack(e));
			}
		}
	}

	public HashMap<String, ObjectInspector> getTagKey2OIs() {
		return this.tagKey2OIs;
	}
}
