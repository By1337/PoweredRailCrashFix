package org.by1337.fix.nms.powered_rail_crash;

import org.by1337.bpatcher.patcher.api.Patch;
import org.by1337.bpatcher.patcher.api.Patcher;
import org.by1337.bpatcher.util.AbstractInsnNodeIterator;
import org.by1337.bpatcher.util.ByteCodeBuilder;
import org.by1337.bpatcher.util.BytecodeHelper;
import org.by1337.bpatcher.util.MethodPrinter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;


@Patch
public class BlockPoweredRailPatch implements Patcher {
    public static int UPDATE_LIMIT = 5;
    public boolean ifAdd;
    public boolean counterIncrement;
    public boolean counterDecrease;

    @Override
    public void apply(ClassNode classNode) {
        FieldNode fieldNode = new FieldNode(Opcodes.ASM9, Opcodes.ACC_PUBLIC, "counter", "I", null, 0);
        classNode.fields.add(fieldNode);
        MethodNode methodNode = BytecodeHelper.getMethod(classNode, "a", "(Lnet/minecraft/server/v1_16_R3/IBlockData;Lnet/minecraft/server/v1_16_R3/World;Lnet/minecraft/server/v1_16_R3/BlockPosition;Lnet/minecraft/server/v1_16_R3/Block;)V");
        if (methodNode == null) {
            throw new IllegalArgumentException("Could not find the right method for the modification!");
        }

        { // counter--;
            AbstractInsnNodeIterator iterator = new AbstractInsnNodeIterator(methodNode.instructions.toArray());
            while (iterator.hasNext()) {
                AbstractInsnNode node = iterator.next();
                if (node.getOpcode() == Opcodes.RETURN) {
                    ByteCodeBuilder builder = new ByteCodeBuilder();

                    builder.aload(0);
                    builder.dup();
                    builder.getfield(classNode, fieldNode);
                    builder.int_(1);
                    builder.isub();
                    builder.putfield(classNode, fieldNode);
                    builder.push(new LabelNode());
                    methodNode.instructions.insertBefore(node, builder.getSource());
                    counterDecrease = true;
                }
            }
        }

       { // if (counter > 5) { return; } else { counter++; }
           ByteCodeBuilder builder = new ByteCodeBuilder();

           LabelNode elseLabel = new LabelNode();

           builder.aload(0);
           builder.getfield(classNode, fieldNode);
           builder.int_(UPDATE_LIMIT);
           builder.if_icmple(elseLabel);
           builder.return_();
           builder.push(elseLabel);

           builder.aload(0);
           builder.dup();
           builder.getfield(classNode, fieldNode);
           builder.int_(1);
           builder.iadd();
           builder.putfield(classNode, fieldNode);
           builder.push(new LabelNode());


           methodNode.instructions.insert(methodNode.instructions.getFirst(), builder.getSource());
           ifAdd = true;
           counterIncrement = true;
       }


       if (!counterDecrease) {
           String methodDump = MethodPrinter.write(methodNode, classNode);
           throw new IllegalStateException(
                   "Failed to apply the patch! ifAdd=" + ifAdd + ", counterIncrement=" + counterIncrement + ", counterDecrease=" + counterDecrease + "\n\n======method dump======\n" + methodDump
           );
       }
    }

    @Override
    public String targetClass() {
        return "net/minecraft/server/v1_16_R3/BlockPoweredRail";
    }

    @Override
    public boolean needRecomputeFrames() {
        return true;
    }
}
