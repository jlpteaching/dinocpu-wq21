// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu.pipelined

import chisel3._
import chisel3.util._
import dinocpu._
import dinocpu.components._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.6 of Patterson and Hennessy
 * This follows figure 4.49
 */
class PipelinedCPU(implicit val conf: CPUConfig) extends BaseCPU {

  // Everything in the register between IF and ID stages
  class IFIDBundle extends Bundle {
    val instruction = UInt(32.W)
    val pc          = UInt(32.W)
  }

  // Control signals used in EX stage
  class EXControl extends Bundle {
  }

  // Control signals used in MEM stage
  class MControl extends Bundle {
  }

  // Control signals used in WB stage
  class WBControl extends Bundle {
  }

  // Data of the the register between ID and EX stages
  class IDEXBundle extends Bundle {
  }

  // Control block of the IDEX register
  class IDEXControl extends Bundle {
    val ex_ctrl  = new EXControl
    val mem_ctrl = new MControl
    val wb_ctrl  = new WBControl
  }

  // Everything in the register between EX and MEM stages
  class EXMEMBundle extends Bundle {
  }

  // Control block of the EXMEM register
  class EXMEMControl extends Bundle {
    val mem_ctrl  = new MControl
    val wb_ctrl   = new WBControl
  }

  // Everything in the register between MEM and WB stages
  class MEMWBBundle extends Bundle {
  }

  // Control block of the MEMWB register
  class MEMWBControl extends Bundle {
    val wb_ctrl = new WBControl
  }

  // All of the structures required
  val pc              = dontTouch(RegInit(0.U))
  val control         = Module(new Control())
  val registers       = Module(new RegisterFile())
  val aluControl      = Module(new ALUControl())
  val alu             = Module(new ALU())
  val immGen          = Module(new ImmediateGenerator())
  val pcPlusFour      = Module(new Adder())
  val nextPCmod       = Module(new NextPC())
  val forwarding      = Module(new ForwardingUnit())  //pipelined only
  val hazard          = Module(new HazardUnit())      //pipelined only
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

  // Remove these as you hook up each one
  registers.io  := DontCare
  aluControl.io := DontCare
  control.io    := DontCare
  alu.io        := DontCare
  immGen.io     := DontCare
  pcPlusFour.io := DontCare
  nextPCmod.io  := DontCare
  io.dmem       := DontCare
  forwarding.io := DontCare
  hazard.io     := DontCare

  id_ex.io       := DontCare
  id_ex_ctrl.io  := DontCare
  ex_mem.io      := DontCare
  ex_mem_ctrl.io := DontCare
  mem_wb.io      := DontCare
  mem_wb_ctrl.io := DontCare

  // Forward declaration of wires that connect different stages

  // From memory back to fetch. Since we don't decide whether to take a branch or not until the memory stage.
  val next_pc = Wire(UInt(32.W))
  next_pc    := DontCare     // Remove when connected

  /////////////////////////////////////////////////////////////////////////////
  // FETCH STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Only update the pc if pcstall is false

  // Send the PC to the instruction memory port to get the instruction
  io.imem.address := pc
  io.imem.valid   := true.B

  // Fill the IF/ID register
  if_id.io.in.instruction := io.imem.instruction
  if_id.io.in.pc          := pc

  // Update during Part III when implementing branches/jump
  if_id.io.valid := true.B
  if_id.io.flush := false.B

  /////////////////////////////////////////////////////////////////////////////
  // ID STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Send opcode to control (line 31 in single-cycle/cpu.scala)

  // Grab rs1 and rs2 from the instruction (line 36 in single-cycle/cpu.scala)

  // Send input from this stage to hazard detection unit (Part III and/or Part IV)

  // Send register numbers to the register file

  // Send the instruction to the immediate generator (line 42 in single-cycle/cpu.scala)

  // Control block of the IDEX register
  // Fill the id_ex register

  // Set the execution control signals

  // Set the memory control signals

  // Set the writeback control signals

  // Set the control signals on the id_ex pipeline register (Part III and/or Part IV)
  id_ex.io.valid := true.B
  id_ex.io.flush := false.B

  id_ex_ctrl.io.valid := true.B
  id_ex_ctrl.io.flush := false.B

  /////////////////////////////////////////////////////////////////////////////
  // EX STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the inputs to the hazard detection unit from this stage (SKIP FOR PART I)

  // Set the input to the forwarding unit from this stage (SKIP FOR PART I)

  // Connect the ALU control wires (line 46 of single-cycle/cpu.scala)

  // Connect the NextPC control wires (line 69 of single-cycle/cpu.scala)

  // Insert the forward inputx mux here (SKIP FOR PART I)

  // Insert the forward inputy mux here (SKIP FOR PART I)

  // Set the ALU operation  (line 51 of single-cycle/cpu.scala)

  // Connect the ALU data wires
  // Input x mux (line 53 of single-cycle/cpu.scala)

  // Input y mux (line 59 of single-cycle/cpu.scala)

  // Connect the NextPC data wires  (line 72 of single-cycle/cpu.scala)

  // Set the EX/MEM register values

  // Determine which result to use (line 79 of single-cycle/cpu.scala)

  // Set the control signals on the ex_mem pipeline register (Part III and/or Part IV)
  ex_mem.io.valid      := true.B
  ex_mem.io.flush      := false.B

  ex_mem_ctrl.io.valid := true.B
  ex_mem_ctrl.io.flush := false.B

  /////////////////////////////////////////////////////////////////////////////
  // MEM STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set data memory IO (line 86 of single-cycle/cpu.scala)

  // Send next_pc back to the fetch stage

  // Send input signals to the hazard detection unit (SKIP FOR PART I)

  // Send input signals to the forwarding unit (SKIP FOR PART I)

  // Wire the MEM/WB register


  // Set the control signals on the mem_wb pipeline register
  mem_wb.io.valid      := true.B
  mem_wb.io.flush      := false.B

  mem_wb_ctrl.io.valid := true.B
  mem_wb_ctrl.io.flush := false.B

  /////////////////////////////////////////////////////////////////////////////
  // WB STAGE
  /////////////////////////////////////////////////////////////////////////////

  // Set the writeback data mux (line 95 single-cycle/cpu.scala)

  // Write the data to the register file

  // Set the input signals for the forwarding unit (SKIP FOR PART I)
}

/*
 * Object to make it easier to print information about the CPU
 */
object PipelinedCPUInfo {
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
      "hazard"
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
