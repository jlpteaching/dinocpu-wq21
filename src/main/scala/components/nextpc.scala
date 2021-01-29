// Logic to calculate the next pc

package dinocpu.components

import chisel3._

/**
 * Next PC unit. This takes various inputs and outputs the next address of the next instruction.
 *
 * Input: branch         true if executing a branch instruction
 * Input: jal            true if executing a jal
 * Input: jalr           true if executing a jalr
 * Input: inputx         first input
 * Input: inputy         second input
 * Input: funct3         the funct3 from the instruction
 * Input: pc             the *current* program counter for this instruction
 * Input: imm            the sign-extended immediate
 *
 * Output: nextpc        the address of the next instruction
 * Output: taken         true if the next pc is not pc+4
 *
 */
class NextPC extends Module {
  val io = IO(new Bundle {
    val branch  = Input(Bool())
    val jal     = Input(Bool())
    val jalr    = Input(Bool())
    val inputx  = Input(UInt(32.W))
    val inputy  = Input(UInt(32.W))
    val funct3  = Input(UInt(3.W))
    val pc      = Input(UInt(32.W))
    val imm     = Input(UInt(32.W))

    val nextpc  = Output(UInt(32.W))
    val taken   = Output(Bool())
  })

  when (io.branch) {
    when (io.funct3 === "b000".U)      { io.taken := io.inputx === io.inputy } // beq
    .elsewhen (io.funct3 === "b001".U) { io.taken := io.inputx =/= io.inputy } // bne
    .elsewhen (io.funct3 === "b100".U) { io.taken := (io.inputx.asSInt < io.inputy.asSInt).asUInt } // blt
    .elsewhen (io.funct3 === "b101".U) { io.taken := (io.inputx.asSInt >= io.inputy.asSInt).asUInt } // bge
    .elsewhen (io.funct3 === "b110".U) { io.taken := io.inputx < io.inputy } // bltu
    .elsewhen (io.funct3 === "b111".U) { io.taken := io.inputx >= io.inputy } // bgeu
    .otherwise                         { io.taken := false.B } // invalid

    when (io.taken) {
      io.nextpc := io.pc + io.imm
    } .otherwise {
      io.nextpc := io.pc + 4.U
    }
  } .elsewhen (io.jal) {
    io.taken := true.B // All jumps are taken
    io.nextpc := io.pc + io.imm
  } .elsewhen (io.jalr) {
    io.taken := true.B // All jumps are taken
    io.nextpc := io.inputx + io.imm
  } .otherwise {
    io.nextpc := io.pc + 4.U
    io.taken  := false.B
  }
}
