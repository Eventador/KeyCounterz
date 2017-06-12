package io.eventador.planes;

import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer08;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;

public class Planes {

    public static void main(String[] args) throws Exception {
        final ParameterTool parameterTool = ParameterTool.fromArgs(args);

        if(parameterTool.getNumberOfParameters() < 4) {
            System.out.println("Missing parameters!\nUsage: Kafka --topic <topic> " +
                    "--bootstrap.servers <kafka brokers> --zookeeper.connect <zk quorum> --group.id <some id>");
            return;
        }

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setRestartStrategy(RestartStrategies.fixedDelayRestart(4, 10000));
        env.enableCheckpointing(5000);
        env.getConfig().setGlobalJobParameters(parameterTool);

        DataStream<String> messageStream = env
                .addSource(new FlinkKafkaConsumer08<>(
                        parameterTool.getRequired("topic"),
                        new SimpleStringSchema(),
                        parameterTool.getProperties()));

        // write kafka stream to standard out.
        messageStream.print();

        env.execute("Kafka Reader Test");
    }

}
