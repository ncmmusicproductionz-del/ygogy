package dev.ygogy.agent;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HookTest {
    /** Stand-in for a game class. */
    public static class FakeCamera {
        public float getZoom() { return 2.0f; }
    }

    @Test
    void floatHookScalesAndOffsetsReturnValue() {
        new AgentBuilder.Default()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .disableClassFormatChanges()
                .type(ElementMatchers.named("dev.ygogy.agent.HookTest$FakeCamera"))
                .transform((b, t, l, m, d) -> b.visit(
                        Advice.to(Hooks.FloatValue.class).on(ElementMatchers.named("getZoom"))))
                .installOn(ByteBuddyAgent.install());

        String id = "dev.ygogy.agent.HookTest$FakeCamera.getZoom";
        FakeCamera cam = new FakeCamera();
        assertEquals(2.0f, cam.getZoom());

        Settings.set("scale:" + id, 1.5);
        assertEquals(3.0f, cam.getZoom());

        Settings.set("offset:" + id, 1.0);
        assertEquals(4.0f, cam.getZoom());
    }
}
