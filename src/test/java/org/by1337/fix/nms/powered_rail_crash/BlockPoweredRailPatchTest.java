package org.by1337.fix.nms.powered_rail_crash;

import org.by1337.bpatcher.util.BytecodeHelper;
import org.by1337.bpatcher.util.MethodPrinter;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockPoweredRailPatchTest {

    @Test
    void apply() throws IOException {
        File resourcesDirectory = new File("src/test/resources");

        ClassNode node = BytecodeHelper.readClass(Files.readAllBytes(resourcesDirectory.toPath().resolve("BlockPoweredRail.class")));
        node.methods.removeIf(m -> !m.desc.equals("(Lnet/minecraft/server/v1_16_R3/IBlockData;Lnet/minecraft/server/v1_16_R3/World;Lnet/minecraft/server/v1_16_R3/BlockPosition;Lnet/minecraft/server/v1_16_R3/Block;)V"));

        BlockPoweredRailPatch poweredRailPatch = new BlockPoweredRailPatch();
        poweredRailPatch.apply(node);

        assertTrue(poweredRailPatch.counterIncrement);
        assertTrue(poweredRailPatch.counterDecrease);
        assertTrue(poweredRailPatch.ifAdd);

        Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicVerifier());

        try {
            analyzer.analyzeAndComputeMaxs(node.name, node.methods.get(0));
        } catch (AnalyzerException e) {
            throw new RuntimeException("Failed to validate byte code!", e);
        }
    }
}