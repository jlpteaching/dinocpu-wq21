// This file contains the hazard detection unit

package dinocpu.components

import chisel3._

/**
 * The hazard detection unit
 *
 * Input:  rs1, the first source register number
 * Input:  rs2, the first source register number
 * Input:  id_prediction, only for branch, if true then BP in ID has predicted TAKEN, if false then BP in ID has predicted NOT TAKEN
 * Input:  idex_memread, true if the instruction in the ID/EX register is going to read from memory
 * Input:  idex_rd, the register number of the destination register for the instruction in the ID/EX register
 * Input:  exmem_taken, if true, then we are using the nextpc in the EX/MEM register, *not* pc+4.
 *
 * Output: pcSel, the value to write to the pc. 0 for PC+4, 1 from memory, 2 from decode, 3 stall
 * Output: if_id_stall,  if true, we should insert a bubble in the IF/ID stage
 * Output: if_id_flush,  if true, set the IF/ID register to 0
 * Output: id_ex_flush,  if true, we should insert a bubble in the ID/EX stage
 * Output: ex_mem_flush, if true, we should insert a bubble in the EX/MEM stage
 *
 */
class HazardUnitBP extends Module {
  val io = IO(new Bundle {
    val rs1           = Input(UInt(5.W))
    val rs2           = Input(UInt(5.W))
    val id_prediction = Input(Bool())
    val idex_memread  = Input(Bool())
    val idex_rd       = Input(UInt(5.W))
    val exmem_taken   = Input(Bool())

    val pcSel        = Output(UInt(2.W))
    val if_id_stall  = Output(Bool())
    val if_id_flush  = Output(Bool())
    val id_ex_flush  = Output(Bool())
    val ex_mem_flush = Output(Bool())

  })

  // default
  io.pcSel        := 0.U
  io.if_id_stall  := false.B
  io.id_ex_flush  := false.B
  io.ex_mem_flush := false.B
  io.if_id_flush  := false.B

  // Your code goes here
  when (io.exmem_taken === true.B) {
    // branch flush
    io.pcSel := 1.U // use the PC from mem stage
    io.if_id_flush  := true.B
    io.id_ex_flush  := true.B
    io.ex_mem_flush := true.B
  } 
  .elsewhen (io.idex_memread === true.B &&
        (io.idex_rd === io.rs1 || io.idex_rd === io.rs2)) {
    // load to use hazard.
    io.pcSel := 3.U
    io.if_id_stall := true.B
    io.id_ex_flush := true.B
  }
  .otherwise {
    // keep default vals
    io.pcSel        := 0.U
    io.if_id_stall  := false.B
    io.id_ex_flush  := false.B
    io.ex_mem_flush := false.B
    io.if_id_flush  := false.B
  }
}
