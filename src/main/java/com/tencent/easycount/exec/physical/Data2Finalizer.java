package com.tencent.easycount.exec.physical;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;

import com.tencent.easycount.conf.ECConfiguration;
import com.tencent.easycount.exec.io.Data2Sink;
import com.tencent.easycount.exec.io.db.Data2SinkDB;
import com.tencent.easycount.exec.io.db.Data2SinkDBNormal;
import com.tencent.easycount.exec.io.hbase.Data2SinkHbase;
import com.tencent.easycount.exec.io.inner.Data2SinkPrint;
import com.tencent.easycount.exec.io.kafka.Data2SinkKafka;
import com.tencent.easycount.exec.io.kafka.KafkaECProducer;
import com.tencent.easycount.exec.io.local.LocalModeUtils;
import com.tencent.easycount.exec.io.redis.Data2SinkRedis;
import com.tencent.easycount.exec.logical.Operator7FS;
import com.tencent.easycount.exec.logical.Operator7FS.Finalized;
import com.tencent.easycount.metastore.Table;
import com.tencent.easycount.metastore.Table.TableType;
import com.tencent.easycount.metastore.TableUtils;

public class Data2Finalizer implements Finalized {
	// private static Logger log =
	// LoggerFactory.getLogger(Data2Finalizer.class);

	final private HashMap<Integer, Data2Sink> tagId2Sinks;
	private final ECConfiguration hconf;
	final private ConcurrentHashMap<String, KafkaECProducer> tubeProducers = new ConcurrentHashMap<String, KafkaECProducer>();
	final private String taskId;
	final private String execId;

	@Override
	public void printStatus(final int printId) {
		for (final Data2Sink sink : this.tagId2Sinks.values()) {
			sink.printStatus(printId);
		}
	}

	public Data2Finalizer(final ECConfiguration hconf, final String taskId,
			final String execId) {
		this.hconf = hconf;
		this.taskId = taskId;
		this.execId = execId;
		this.tagId2Sinks = new HashMap<Integer, Data2Sink>();
	}

	public void start() {
		for (final KafkaECProducer producer : this.tubeProducers.values()) {
			producer.start();
		}
	}

	public void addFsOp(final Operator7FS fsop) {
		// if (hconf.getBoolean("localmode", false)
		// && fsop.getOpDesc().getTable().getTableType() != TableType.redis) {
		// this.tagId2Sinks.put(fsop.getOpDesc().getOpTagIdx(),
		// new Data2SinkTest(fsop.getOpDesc()));
		final Table tbl = fsop.getOpDesc().getTable();
		if ((tbl.getTableType() != TableType.print)
				&& this.hconf.getBoolean("localmode", false)) {
			this.tagId2Sinks
					.put(fsop.getOpDesc().getOpTagIdx(),
							LocalModeUtils.generateLocalDataSink(tbl,
									fsop.getOpDesc()));
		} else {
			Data2Sink dataSink = null;
			if (tbl.getTableType() == TableType.print) {
				dataSink = new Data2SinkPrint(fsop.getOpDesc());
			} else if ((tbl.getTableType() == TableType.tpg)
					|| (tbl.getTableType() == TableType.mysql)) {
				final boolean mysqlInsertModeNormal = TableUtils
						.getMysqlInsertMode(tbl);
				if (mysqlInsertModeNormal) {
					dataSink = new Data2SinkDBNormal(fsop.getOpDesc());
				} else {
					dataSink = new Data2SinkDB(fsop.getOpDesc());
				}
			} else if (tbl.getTableType() == TableType.redis) {
				dataSink = new Data2SinkRedis(fsop.getOpDesc());
			} else if (tbl.getTableType() == TableType.hbase) {
				dataSink = new Data2SinkHbase(fsop.getOpDesc());
			} else if (tbl.getTableType() == TableType.kafka) {
				final String tubeMaster = TableUtils.getTableTubeMaster(fsop
						.getOpDesc().getTable());
				final int tubePort = TableUtils.getTableTubePort(fsop
						.getOpDesc().getTable());
				final String topic = TableUtils.getTableTopic(fsop.getOpDesc()
						.getTable());

				final String producerid = tubeMaster + "-" + tubePort + "-"
						+ topic;
				if (!this.tubeProducers.containsKey(producerid)) {
					this.tubeProducers.put(producerid, new KafkaECProducer(
							this.hconf, topic, this.taskId, this.execId));
				}

				dataSink = new Data2SinkKafka(this.hconf, fsop.getOpDesc(),
						this.tubeProducers.get(producerid));
			}

			this.tagId2Sinks.put(fsop.getOpDesc().getOpTagIdx(), dataSink);
		}
		fsop.setFinalizer(this);
	}

	@Override
	public boolean finalize(final Object row,
			final ObjectInspector objectInspector,
			final ObjectInspector keyInspector,
			final ObjectInspector attrsInspector, final int opTagIdx) {
		this.tagId2Sinks.get(opTagIdx).finalize(row, objectInspector,
				keyInspector, attrsInspector, opTagIdx);
		return true;
	}

	// @SuppressWarnings("rawtypes")
	// @Override
	// public boolean finalize(Object row, ObjectInspector objectInspector,
	// ExprNodeEvaluator keyEvaluator, ObjectInspector keyInspector,
	// ExprNodeEvaluator attrsEvaluator, ObjectInspector attrsInspector,
	// int opTagIdx) {
	// try {
	// this.tagId2Sinks.get(opTagIdx).finalize(row, objectInspector,
	// keyEvaluator, keyInspector, attrsEvaluator, attrsInspector,
	// opTagIdx);
	// } catch (Throwable e) {
	// log.warn(row + " : " + TDBankUtils.getExceptionStack(e));
	// return false;
	// }
	// return true;
	// }

	@Override
	public void close() throws IOException {
		for (final Data2Sink sink : this.tagId2Sinks.values()) {
			sink.close();
		}
	}
}
