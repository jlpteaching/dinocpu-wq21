// Control logic for the processor

package dinocpu.components

import chisel3._
import chisel3.util.{BitPat, ListLookup}

/**
 * Main control logic for our simple processor
 *
 * Input: opcode:     Opcode from instruction
 *
 * Output: itype         True if we're working on an itype instruction
 * Output: aluop         0 for ld/st, 1 for R-type and jalr
 * Output: ysrc          source for the second ALU/nextpc input (0 is readdata2 and 1 is immediate)
 * Output: xsrc          source for the first ALU/nextpc input (0 is readdata1, 1 is pc)
 * Output: branch        true if branch
 * Output: jal           true if a jal
 * Output: jalr          true if a jalr instruction
 * Output: plus4         true if ALU should add 4 to inputx
 * Output: resultselect  0 for result from alu, 1 for immediate
 * Output: memop         00 if not using memory, 10 if reading, and 11 if writing
 * Output: toreg         0 for result from execute, 1 for data from memory
 * Output: regwrite      true if writing to the register file
 * Output: validinst     True if the instruction we're decoding is valid
 *
 * For more information, see section 4.4 of Patterson and Hennessy.
 * This follows figure 4.22 somewhat.
 */

class Control extends Module {
  val io = IO(new Bundle {
    val opcode = Input(UInt(7.W))

    val itype        = Output(Bool())
    val aluop        = Output(Bool())
    val xsrc         = Output(Bool())
    val ysrc         = Output(Bool())
    val branch       = Output(Bool())
    val jal          = Output(Bool())
    val jalr         = Output(Bool())
    val plus4        = Output(Bool())
    val resultselect = Output(Bool())
    val memop        = Output(UInt(2.W))
    val toreg        = Output(Bool())
    val regwrite     = Output(Bool())
    val validinst    = Output(Bool())
  })

  val signals =
    ListLookup(io.opcode,
      /*default*/           List(false.B, false.B, false.B, false.B, false.B, false.B, false.B, false.B, false.B,      0.U,   false.B, false.B,  false.B),

      Array(              /*     itype,   aluop,   xsrc,    ysrc,    branch,  jal,     jalr     plus4,   resultselect, memop, toreg,   regwrite, validinst */
      // R-format
      BitPat("b0110011") -> List(false.B, true.B,  false.B, false.B, false.B, false.B, false.B, false.B, false.B,      0.U,   false.B, true.B,   true.B),

      // Your code coes here for Lab 2.
      // Remember to make sure to have commas at the end of each line

      ) // Array
    ) // ListLookup

  io.itype        := signals(0)
  io.aluop        := signals(1)
  io.xsrc         := signals(2)
  io.ysrc         := signals(3)
  io.branch       := signals(4)
  io.jal          := signals(5)
  io.jalr         := signals(6)
  io.plus4        := signals(7)
  io.resultselect := signals(8)
  io.memop        := signals(9)
  io.toreg        := signals(10)
  io.regwrite     := signals(11)
  io.validinst    := signals(12)
}
