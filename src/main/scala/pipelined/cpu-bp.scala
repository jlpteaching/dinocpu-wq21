// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu.pipelined

import chisel3._
import chisel3.util._
import dinocpu._
import dinocpu.components._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 */
class PipelinedCPUBP(implicit val conf: CPUConfig) extends BaseCPU {

  // Everything in the register between IF and ID stages
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc          = UInt(32.W)
  }

  // Control signals used in EX stage

  class EXControl extends Bundle {
    val itype        = Bool()
    val aluop        = Bool()
    val resultselect = Bool()
    val xsrc         = Bool()
    val ysrc         = Bool()
    val plus4        = Bool()
    val branch       = Bool()
    val jal          = Bool()
    val jalr         = Bool()
    val prediction   = Bool()
  }

  // Control signals used in MEM stage
  class MControl extends Bundle {
    val memop = UInt(2.W)
  }

  // Control signals used in WB stage
  class WBControl extends Bundle {
    val toreg    = Bool()
    val regwrite = Bool()
  }

  // Data of the the register between ID and EX stages
  class IDEXBundle extends Bundle {
    val pc          = UInt(32.W)
    val instruction = UInt(32.W)
    val sextImm     = UInt(32.W)
    val readdata1   = UInt(32.W)
    val readdata2   = UInt(32.W)
  }

  // Control block of the IDEX register
  class IDEXControl extends Bundle {
    val ex_ctrl  = new EXControl
    val mem_ctrl = new MControl
    val wb_ctrl  = new WBControl
  }

  // Everything in the register between EX and MEM stages
  class EXMEMBundle extends Bundle {
    val ex_result     = UInt(32.W)
    val mem_writedata = UInt(32.W)
    val instruction   = UInt(32.W)
    val next_pc       = UInt(32.W)
    val taken         = Bool()
  }

  // Control block of the EXMEM register
  class EXMEMControl extends Bundle {
    val mem_ctrl  = new MControl
    val wb_ctrl   = new WBControl
  }

  // Everything in the register between MEM and WB stages
  class MEMWBBundle extends Bundle {
    val instruction = UInt(32.W)
    val readdata    = UInt(32.W)
    val ex_result   = UInt(32.W)
  }

  // Control block of the MEMWB register
  class MEMWBControl extends Bundle {
    val wb_ctrl = new WBControl
  }

  // All of the structures required
  val pc              = RegInit(0.U)
  val control         = Module(new Control())
  val registers       = Module(new RegisterFile())
  val aluControl      = Module(new ALUControl())
  val alu             = Module(new ALU())
  val immGen          = Module(new ImmediateGenerator())
  val pcPlusFour      = Module(new Adder())
  val nextPCmod       = Module(new NextPC())
  val forwarding      = Module(new ForwardingUnit())  //pipelined only
  val hazard          = Module(new HazardUnitBP())    //pipelined only
  val predictor       = Module(conf.getBranchPredictor)
  val branchAdd       = Module(new Adder())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  // The four pipeline registers
  val if_id       = Module(new StageReg(new IFIDBundle))

  val id_ex       = Module(new StageReg(new IDEXBundle))
  val id_ex_ctrl  = Module(new StageReg(new IDEXControl))

  val ex_mem      = Module(new StageReg(new EXMEMBundle))
  val ex_mem_ctrl = Module(new StageReg(new EXMEMControl))

  val mem_wb      = Module(new StageReg(new MEMWBBundle))

  // To make the interface of the mem_wb_ctrl register consistent with the other control
  // registers, we create an anonymous Bundle
  val mem_wb_ctrl = Module(new StageReg(new MEMWBControl))

  val bpCorrect   = RegInit(0.U(32.W))
  val bpIncorrect = RegInit(0.U(32.W))
  when (bpCorrect > (1.U << 20)) {
    // Force these wires not to disappear
    printf(p"BP correct: $bpCorrect; incorrect: $bpIncorrect\n")
  }
  
  // Remove these as you hook up each one
  // registers.io  := DontCare
  // aluControl.io := DontCare
  // alu.io        := DontCare
  // immGen.io     := DontCare
  // pcPlusFour.io := DontCare
  // io.dmem       := DontCare
  // forwarding.io := DontCare
  // hazard.io     := DontCare

  // id_ex.io       := DontCare
  // id_ex_ctrl.io  := DontCare
  // ex_mem.io      := DontCare
  // ex_mem_ctrl.io := DontCare
  // mem_wb.io      := DontCare
  // mem_wb_ctrl.io := DontCare

  // Forward declaration of wires that connect different stages

  // From decode, execute, memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val next_pc    = Wire(UInt(32.W))
  val id_next_pc = Wire(UInt())
  val write_data = Wire(UInt())
  // next_pc    := DontCare     // Remove when connected

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Select the proper next pc val according to pcSel
  pc := MuxCase(0.U, Array(
            (hazard.io.pcSel === 0.U) -> pcPlusFour.io.result,
            (hazard.io.pcSel === 1.U) -> next_pc,
            (hazard.io.pcSel === 2.U) -> id_next_pc,
            (hazard.io.pcSel === 3.U) -> pc))
  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc
  io.imem.valid   := true.B

  // Get the PC + 4
  pcPlusFour.io.inputx := pc
  pcPlusFour.io.inputy := 4.U

  // Fill the IF/ID register
  if_id.io.in.instruction := io.imem.instruction
  if_id.io.in.pc          := pc

  // Update during Part III when implementing branches/jump
  if_id.io.valid := !hazard.io.if_id_stall
  if_id.io.flush := hazard.io.if_id_flush

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Send opcode to control (line 31 in single-cycle/cpu.scala)
  control.io.opcode := if_id.io.data.instruction(6,0)

  // Grab rs1 and rs2 from the instruction (line 36 in single-cycle/cpu.scala)
  val rs1 = if_id.io.data.instruction(19,15)
  val rs2 = if_id.io.data.instruction(24,20)

  // Send input from this stage to hazard detection unit (Part III only)
  hazard.io.rs1 := rs1
  hazard.io.rs2 := rs2

  // Send register numbers to the register file (line 36 in single-cycle/cpu.scala)
  registers.io.readreg1 := rs1
  registers.io.readreg2 := rs2
  registers.io.writedata := write_data

  // Send the instruction to the immediate generator (line 42 in single-cycle/cpu.scala)
  immGen.io.instruction := if_id.io.data.instruction

  // Connect the branchAdd unit
  branchAdd.io.inputx := if_id.io.data.pc
  branchAdd.io.inputy := immGen.io.sextImm
  // Send the PC back to fetch
  id_next_pc := branchAdd.io.result

  // Set the predictor inputs
  predictor.io.pc := if_id.io.data.pc

  // Set the branch for this stage to the hazard
  // This is needed for when the branch is predicted taken 
  hazard.io.id_prediction := control.io.branch && predictor.io.prediction

  // Control block of the IDEX register

  // Fill the id_ex register
  id_ex.io.in.pc          := if_id.io.data.pc
  id_ex.io.in.sextImm     := immGen.io.sextImm
  id_ex.io.in.instruction := if_id.io.data.instruction
  id_ex.io.in.readdata1   := registers.io.readdata1
  id_ex.io.in.readdata2   := registers.io.readdata2

  // Set the execution control signals
  id_ex_ctrl.io.in.ex_ctrl.aluop        := control.io.aluop
  id_ex_ctrl.io.in.ex_ctrl.itype        := control.io.itype
  id_ex_ctrl.io.in.ex_ctrl.resultselect := control.io.resultselect
  id_ex_ctrl.io.in.ex_ctrl.xsrc         := control.io.xsrc
  id_ex_ctrl.io.in.ex_ctrl.ysrc         := control.io.ysrc
  id_ex_ctrl.io.in.ex_ctrl.plus4        := control.io.plus4
  id_ex_ctrl.io.in.ex_ctrl.branch       := control.io.branch
  id_ex_ctrl.io.in.ex_ctrl.jal          := control.io.jal
  id_ex_ctrl.io.in.ex_ctrl.jalr         := control.io.jalr
  id_ex_ctrl.io.in.ex_ctrl.prediction   := predictor.io.prediction

  // Set the memory control signals
  id_ex_ctrl.io.in.mem_ctrl.memop := control.io.memop

  // Set the writeback control signals
  id_ex_ctrl.io.in.wb_ctrl.regwrite := control.io.regwrite
  id_ex_ctrl.io.in.wb_ctrl.toreg    := control.io.toreg

  id_ex.io.valid := true.B
  id_ex.io.flush := hazard.io.id_ex_flush

  id_ex_ctrl.io.valid := true.B
  id_ex_ctrl.io.flush := hazard.io.id_ex_flush

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the inputs to the hazard detection unit from this stage (SKIP FOR PART I)
  hazard.io.idex_rd := id_ex.io.data.instruction(11,7)

  when (id_ex_ctrl.io.data.mem_ctrl.memop === 2.U) {
    hazard.io.idex_memread := true.B
  } .otherwise {
    hazard.io.idex_memread := false.B
  }

  // Set the input to the forwarding unit from this stage (SKIP FOR PART I)
  forwarding.io.rs1     := id_ex.io.data.instruction(19,15)
  forwarding.io.rs2     := id_ex.io.data.instruction(24,20)
  forwarding.io.exmemrd := ex_mem.io.data.instruction(11,7)
  forwarding.io.exmemrw := ex_mem_ctrl.io.data.wb_ctrl.regwrite

  // Connect the ALU control wires (line 46 of single-cycle/cpu.scala)
  aluControl.io.itype  := id_ex_ctrl.io.data.ex_ctrl.itype
  aluControl.io.aluop  := id_ex_ctrl.io.data.ex_ctrl.aluop
  aluControl.io.funct3 := id_ex.io.data.instruction(14,12)
  aluControl.io.funct7 := id_ex.io.data.instruction(31,25)

  // Connect the NextPC control wires (line 69 of single-cycle/cpu.scala)
  nextPCmod.io.branch := id_ex_ctrl.io.data.ex_ctrl.branch
  nextPCmod.io.jal    := id_ex_ctrl.io.data.ex_ctrl.jal
  nextPCmod.io.jalr   := id_ex_ctrl.io.data.ex_ctrl.jalr

  // Insert the forward inputx mux here (SKIP FOR PART I)
  val forward_a_mux = Wire(UInt(32.W))

  when (forwarding.io.forwardA === 0.U) {
    forward_a_mux := id_ex.io.data.readdata1
  } .elsewhen (forwarding.io.forwardA === 1.U) {
    forward_a_mux := ex_mem.io.data.ex_result
  } .otherwise { // forwarding.io.forwardA === 2.U
    forward_a_mux := write_data
  }

  // Insert the forward inputy mux here (SKIP FOR PART I)
  val forward_b_mux = Wire(UInt(32.W))

  when (forwarding.io.forwardB === 0.U) {
    forward_b_mux := id_ex.io.data.readdata2
  } .elsewhen (forwarding.io.forwardB === 1.U) {
    forward_b_mux := ex_mem.io.data.ex_result
  } .otherwise { // forwarding.io.forwardB === 2.U
    forward_b_mux := write_data
  }

  // Set the ALU operation  (line 51 of single-cycle/cpu.scala)
  alu.io.operation := aluControl.io.operation

  // Connect the ALU data wires
  // Input x mux (line 53 of single-cycle/cpu.scala)
  when (id_ex_ctrl.io.data.ex_ctrl.xsrc === true.B) {
    alu.io.inputx := id_ex.io.data.pc
  } .otherwise {
    alu.io.inputx := forward_a_mux
  }
  // Input y mux (line 59 of single-cycle/cpu.scala)
  when (id_ex_ctrl.io.data.ex_ctrl.plus4 === true.B) {
    alu.io.inputy := 4.U
  } .otherwise {
    when (id_ex_ctrl.io.data.ex_ctrl.ysrc === true.B) {
      alu.io.inputy := id_ex.io.data.sextImm
    } .otherwise {
      alu.io.inputy := forward_b_mux
    }
  }

  // Connect the NextPC data wires  (line 72 of single-cycle/cpu.scala)
  nextPCmod.io.inputx := forward_a_mux
  nextPCmod.io.inputy := forward_b_mux
  nextPCmod.io.funct3 := id_ex.io.data.instruction(14,12)
  nextPCmod.io.pc     := id_ex.io.data.pc
  nextPCmod.io.imm    := id_ex.io.data.sextImm

  // Set the EX/MEM register values
  ex_mem.io.in.instruction   := id_ex.io.data.instruction
  ex_mem.io.in.mem_writedata := forward_b_mux

  ex_mem_ctrl.io.in.mem_ctrl.memop   := id_ex_ctrl.io.data.mem_ctrl.memop
  ex_mem_ctrl.io.in.wb_ctrl.regwrite := id_ex_ctrl.io.data.wb_ctrl.regwrite
  ex_mem_ctrl.io.in.wb_ctrl.toreg    := id_ex_ctrl.io.data.wb_ctrl.toreg

  // Calculate whether which PC we should use and set the taken flag
  ex_mem.io.in.next_pc := nextPCmod.io.nextpc
  ex_mem.io.in.taken   := nextPCmod.io.taken

  // Determine which result to use (line 79 of single-cycle/cpu.scala)
  when (id_ex_ctrl.io.data.ex_ctrl.resultselect === 0.U) {
    ex_mem.io.in.ex_result := alu.io.result
  } .otherwise {
    ex_mem.io.in.ex_result := id_ex.io.data.sextImm
  }
// ************** Logic to drive proper taken while using a branch predictor **************//
// Update the branch predictor
  when (id_ex_ctrl.io.data.ex_ctrl.branch && ~hazard.io.ex_mem_flush) {
    // when it's a branch, update the branch predictor
    predictor.io.update := true.B
    predictor.io.taken  := nextPCmod.io.taken

    // Update the branch predictor stats
    when (id_ex_ctrl.io.data.ex_ctrl.prediction === nextPCmod.io.taken) {
      bpCorrect   := bpCorrect + 1.U
    } 
    .otherwise {
      bpIncorrect := bpIncorrect + 1.U
    }

  }
  .otherwise {
    // If not a branch, don't update
    predictor.io.update := false.B
    //predictor.io.taken  := false.B
    predictor.io.taken  := DontCare
  }

  // No need to do anything unless the prediction was wrong
  when (id_ex_ctrl.io.data.ex_ctrl.branch){
    when(id_ex_ctrl.io.data.ex_ctrl.prediction =/= nextPCmod.io.taken) {
      ex_mem.io.in.taken := true.B
    } 
    .otherwise {
      ex_mem.io.in.taken := false.B
    }
  }
  // ************** End of logic to drive proper taken while using a branch predictor **************//

  ex_mem.io.valid      := true.B
  ex_mem.io.flush      := hazard.io.ex_mem_flush
  ex_mem_ctrl.io.valid := true.B
  ex_mem_ctrl.io.flush := hazard.io.ex_mem_flush

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set data memory IO (line 86 of single-cycle/cpu.scala)
  io.dmem.address   := ex_mem.io.data.ex_result
  io.dmem.memread   := ex_mem_ctrl.io.data.mem_ctrl.memop === 2.U
  io.dmem.memwrite  := ex_mem_ctrl.io.data.mem_ctrl.memop === 3.U
  io.dmem.valid     := ex_mem_ctrl.io.data.mem_ctrl.memop(1)
  io.dmem.maskmode  := ex_mem.io.data.instruction(13,12)
  io.dmem.sext      := ~ex_mem.io.data.instruction(14)
  io.dmem.writedata := ex_mem.io.data.mem_writedata

  // Send next_pc back to the fetch stage
  next_pc := ex_mem.io.data.next_pc

  // Send input signals to the hazard detection unit (SKIP FOR PART I)
  hazard.io.exmem_taken := ex_mem.io.data.taken

  // Send input signals to the forwarding unit (SKIP FOR PART I)

  // Wire the MEM/WB register
  mem_wb.io.in.ex_result   := ex_mem.io.data.ex_result
  mem_wb.io.in.instruction := ex_mem.io.data.instruction
  mem_wb.io.in.readdata    := io.dmem.readdata

  mem_wb_ctrl.io.in.wb_ctrl.regwrite := ex_mem_ctrl.io.data.wb_ctrl.regwrite
  mem_wb_ctrl.io.in.wb_ctrl.toreg    := ex_mem_ctrl.io.data.wb_ctrl.toreg

  // Set the control signals on the mem_wb pipeline register
  mem_wb.io.valid      := true.B
  mem_wb.io.flush      := false.B
  mem_wb_ctrl.io.valid := true.B
  mem_wb_ctrl.io.flush := false.B
  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the writeback data mux (line 95 single-cycle/cpu.scala)
  when (mem_wb.io.data.instruction(11,7) === 0.U) {
    registers.io.wen := false.B
  } .otherwise {
    registers.io.wen := mem_wb_ctrl.io.data.wb_ctrl.regwrite
  }

  registers.io.writereg := mem_wb.io.data.instruction(11,7)

  // Write the data to the register file
  when (mem_wb_ctrl.io.data.wb_ctrl.toreg === true.B) {
    write_data := mem_wb.io.data.readdata
  } .otherwise {
    write_data := mem_wb.io.data.ex_result
  }

  // Set the input signals for the forwarding unit (SKIP FOR PART I)
  forwarding.io.memwbrd := mem_wb.io.data.instruction(11,7)
  forwarding.io.memwbrw := mem_wb_ctrl.io.data.wb_ctrl.regwrite
}

/*
 * Object to make it easier to print information about the CPU
 */
object PipelinedCPUBPInfo {
  def getModules(): List[String] = {
    List(
      "imem",
      "dmem",
      "control",
      "registers",
      "aluControl",
      "alu",
      "immGen",
      "pcPlusFour",
      "nextPCmod",
      "forwarding",
      "hazard",
      "predictor",
      "branchAdd"
    )
  }
  def getPipelineRegs(): List[String] = {
    List(
      "if_id",
      "id_ex",
      "id_ex_ctrl",
      "ex_mem",
      "ex_mem_ctrl",
      "mem_wb",
      "mem_wb_ctrl"
    )
  }
}
