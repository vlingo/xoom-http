package io.vlingo.http.resource;

import org.junit.Test;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.results.RunResult;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;

public class BenchmarkTests {
  @Test
  public void launchBenchmark() throws Exception {
    final double expectedMinUsPerOp = 570;

    Options opt = new OptionsBuilder()
      .include(this.getClass().getSimpleName() + "\\.benchmark.*")
      .mode(Mode.AverageTime)
      .timeUnit(TimeUnit.MICROSECONDS)
      .warmupTime(TimeValue.seconds(1))
      .warmupIterations(2)
      .measurementTime(TimeValue.seconds(1))
      .measurementIterations(2)
      .threads(1)
      .forks(1)
      .shouldFailOnError(true)
      .shouldDoGC(true)
      .build();

    RunResult result = new Runner(opt).runSingle();
    double usPerOp = result.getPrimaryResult().getScore();
    assertTrue("µs/matching operation = " + usPerOp + " is higher than " + expectedMinUsPerOp,
      usPerOp < expectedMinUsPerOp);
  }

  @State(Scope.Thread)
  public static class ActionMatchingBenchmarkState {
    Map<URI, Action> subjects;

    @Setup(Level.Trial)
    public void
    initialize() {
      subjects = new HashMap<>();
      for (int i = 0; i < 1000; i++)
        subjects.put(
          URI.create("/param1/" + UUID.randomUUID() + "/param2/" + UUID.randomUUID() + "/"),
          new Action(
            0,
            "GET",
            "/param1/{p1}/param2/{p2}/",
            "foo(String p1, String p2)",
            null)
        );
      subjects.put(
        URI.create("/a/" + UUID.randomUUID() + "/b/" + UUID.randomUUID() + "/c/" + UUID.randomUUID() + "/d/" + UUID.randomUUID()
          + "/e/" + UUID.randomUUID() + "/f/" + UUID.randomUUID() + "/g/" + UUID.randomUUID() + "/h/" + UUID.randomUUID()
          + "/i/" + UUID.randomUUID() + "/j/" + UUID.randomUUID() + "/k/" + UUID.randomUUID() + "/l/" + UUID.randomUUID()),
        new Action(
          0,
          "GET",
          "/a/{a}/b/{b}/c/{c}/d/{d}/e/{e}/f/{f}/g/{g}/h/{h}/i/{i}/j/{j}/k/{k}/l/{l}",
          "foo(String a, String b, String c, String d, String e, String f, String g, String h, String i, String j, String k, String l)",
          null)
      );
    }
  }

  @Benchmark
  public void benchmarkActionMatching(ActionMatchingBenchmarkState state, Blackhole bh) {
    Map<URI, Action> subjects = state.subjects;

    for (Map.Entry<URI, Action> e : subjects.entrySet()) {
      Action a = e.getValue();
      bh.consume(a.matchWith(a.method, e.getKey()));
    }
  }
}
