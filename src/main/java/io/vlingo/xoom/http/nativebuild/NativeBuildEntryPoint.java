package io.vlingo.xoom.http.nativebuild;

import io.vlingo.xoom.actors.World;
import io.vlingo.xoom.http.Filters;
import io.vlingo.xoom.http.resource.Configuration;
import io.vlingo.xoom.http.resource.Server;
import org.graalvm.nativeimage.c.function.CEntryPoint;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class NativeBuildEntryPoint {
  @CEntryPoint(name = "Java_io_vlingo_xoom_httpnative_Native_start")
  public static int start(@CEntryPoint.IsolateThreadContext long isolateId, CCharPointer name) {
    final String nameString = CTypeConversion.toJavaString(name);
    World world = World.startWithDefaults(nameString);

    Server server =
      Server.startWith(
        world.stage(),
        null,
        Filters.none(),
        8081,
        Configuration.Sizing.defineWith(4, 10, 100, 10240),
        Configuration.Timing.defineWith(3, 1, 100),
        "arrayQueueMailbox",
        "arrayQueueMailbox");
    return 0;
  }
}
