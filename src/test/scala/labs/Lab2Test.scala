// Tests for Lab 2. Feel free to modify and add more tests here.
// If you name your test class something that ends with "TesterLab2" it will
// automatically be run when you use `Lab2 / test` at the sbt prompt.

package dinocpu

import dinocpu._
import chisel3._
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import dinocpu.components._
import dinocpu.test._
import dinocpu.test.components._


/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleRTypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleRTypeTesterLab2'
  * }}}
  */
class SingleCycleRTypeTesterLab2 extends CPUFlatSpec {
  behavior of "Single Cycle CPU"
  for (test <- InstTests.rtype) {
    it should s"run R-type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleITypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleITypeTesterLab2'
  * }}}
  *
*/
class SingleCycleITypeTesterLab2 extends CPUFlatSpec {

  val maxInt = BigInt("FFFFFFFF", 16)

  def twoscomp(v: BigInt) : BigInt = {
    if (v < 0) {
      return maxInt + v + 1
    } else {
      return v
    }
  }

  val tests = InstTests.tests("itype") ++ InstTests.tests("itypeMultiCycle")
  for (test <- tests) {
    "Single Cycle CPU" should s"run I-Type instruction ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleLoadTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleLoadTesterLab2'
  * }}}
  *
*/
class SingleCycleLoadTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    InstTests.nameMap("lw1"), InstTests.nameMap("lwfwd")
 )
  for (test <- tests) {
    "Single Cycle CPU" should s"run load instruction test ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleUTypeTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleUTypeTesterLab2'
  * }}}
  *
*/
class SingleCycleUTypeTesterLab2 extends CPUFlatSpec {

  val tests = InstTests.tests("utype") ++ InstTests.tests("utypeMultiCycle")
  for (test <- tests) {
  "Single Cycle CPU" should s"run auipc/lui instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleStoreTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleStoreTesterLab2'
  * }}}
  *
*/
class SingleCycleStoreTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    InstTests.nameMap("sw")
 )
  for (test <- tests) {
  "Single Cycle CPU" should s"run add Store instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleLoadStoreTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleLoadStoreTesterLab2'
  * }}}
  *
*/
class SingleCycleLoadStoreTesterLab2 extends CPUFlatSpec {

  val tests = InstTests.tests("memory")
  for (test <- tests) {
  "Single Cycle CPU" should s"run load/store instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleBranchTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleBranchTesterLab2'
  * }}}
  *
*/
class SingleCycleBranchTesterLab2 extends CPUFlatSpec {
  behavior of "Single Cycle CPU"
  for (test <- InstTests.branch) {
    it should s"run branch instruction test ${test.binary}${test.extraName}" in {
      CPUTesterDriver(test, "single-cycle") should be(true)
    }
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleJALTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleJALTesterLab2'
  * }}}
  *
*/
class SingleCycleJALTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    InstTests.nameMap("jal")
)
  for (test <- tests) {
  "Single Cycle CPU" should s"run JAL instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleJALRTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleJALRTesterLab2'
  * }}}
  *
*/
class SingleCycleJALRTesterLab2 extends CPUFlatSpec {

  val tests = List[CPUTestCase](
    InstTests.nameMap("jalr0"), InstTests.nameMap("jalr1")
 )
  for (test <- tests) {
  "Single Cycle CPU" should s"run JALR instruction test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.SingleCycleApplicationsTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.SingleCycleApplicationsTesterLab2'
  * }}}
  *
*/
class SingleCycleApplicationsTesterLab2 extends CPUFlatSpec {

  val tests = InstTests.tests("smallApplications")
  for (test <- tests) {
  "Single Cycle CPU" should s"run application test ${test.binary}${test.extraName}" in {
    CPUTesterDriver(test, "single-cycle") should be(true)
	}
  }
}

// Unit tests for the main control logic

/*
**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * testOnly dinocpu.ControlTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'testOnly dinocpu.ControlTesterLab2'
  * }}}
  */
class ControlTesterLab2 extends ChiselFlatSpec {
  "Control" should s"match expectations" in {
    Driver(() => new Control) {
      c => new ControlUnitTester(c)
    } should be (true)
  }
}

class ALUControlUnitBTypeTester(c: ALUControl) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // alu,   itype,    Funct7,       Func3,    Control Input
	  //(  false.B, false.B, "b0000000".U, "b000".U, "b0111".U, "load/store"),
    //(  false.B, false.B, "b1111111".U, "b111".U, "b0111".U, "load/store"),
    //(  false.B, false.B, "b0000000".U, "b000".U, "b0111".U, "load/store"),
    (  true.B,  false.B, "b0000000".U, "b000".U, "b0111".U, "add"),
    (  true.B,  false.B, "b0100000".U, "b000".U, "b0100".U, "sub"),
    (  true.B,  false.B, "b0000000".U, "b001".U, "b1000".U, "sll"),
    (  true.B,  false.B, "b0000000".U, "b010".U, "b1001".U, "slt"),
    (  true.B,  false.B, "b0000000".U, "b011".U, "b0001".U, "sltu"),
    (  true.B,  false.B, "b0000000".U, "b100".U, "b0000".U, "xor"),
    (  true.B,  false.B, "b0000000".U, "b101".U, "b0010".U, "srl"),
    (  true.B,  false.B, "b0100000".U, "b101".U, "b0011".U, "sra"),
    (  true.B,  false.B, "b0000000".U, "b110".U, "b0101".U, "or"),
    (  true.B,  false.B, "b0000000".U, "b111".U, "b0110".U, "and"),
    (  true.B,  true.B,  "b0000000".U, "b000".U, "b0111".U, "addi"),
    (  true.B,  true.B,  "b0000000".U, "b010".U, "b1001".U, "slti"),
    (  true.B,  true.B,  "b0000000".U, "b011".U, "b0001".U, "sltiu"),
    (  true.B,  true.B,  "b0000000".U, "b100".U, "b0000".U, "xori"),
    (  true.B,  true.B,  "b0000000".U, "b110".U, "b0101".U, "ori"),
    (  true.B,  true.B,  "b0000000".U, "b111".U, "b0110".U, "andi"),
    (  true.B,  true.B,  "b0000000".U, "b001".U, "b1000".U, "slli"),
    (  true.B,  true.B,  "b0000000".U, "b101".U, "b0010".U, "srli"),
    (  true.B,  true.B,  "b0100000".U, "b101".U, "b0011".U, "srai")
  )

  for (t <- tests) {
    poke(ctl.io.aluop, t._1)
    poke(ctl.io.itype, t._2)
    poke(ctl.io.funct7, t._3)
    poke(ctl.io.funct3, t._4)
    step(1)
    expect(ctl.io.operation, t._5, s"${t._6} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.ALUControlTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.ALUControlTesterLab2'
  * }}}
  */
class ALUControlTesterLab2 extends ChiselFlatSpec {
  "ALUControl" should s"match expectations for each intruction type" in {
    Driver(() => new ALUControl) {
      c => new ALUControlUnitBTypeTester(c)
    } should be (true)
  }
} 

class NextPCBrTester(c: NextPC) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // branch,   jal,    jalr,   inputx, inputy, funct3,    pc,    imm,   nextpc, taken
	  (  true.B, false.B, false.B, 13.U,    9.U,   "b000".U,  20.U ,  16.U,  24.U,  false.B, "beq0"),
    (  true.B, false.B, false.B, 133.U,   133.U, "b000".U,  20.U ,  16.U,  36.U,  true.B,  "beq1"),
    (  true.B, false.B, false.B, 13.U,    9.U,   "b001".U,  20.U ,  16.U,  36.U,  true.B,  "bne"),
    (  true.B, false.B, false.B, 13.U,    9.U,   "b100".U,  20.U ,  16.U,  24.U,  false.B, "blt"),
    (  true.B, false.B, false.B, 13.U,    9.U,   "b101".U,  20.U ,  16.U,  36.U,  true.B,  "bge"),
    (  true.B, false.B, false.B, 13.U,    9.U,   "b110".U,  20.U ,  16.U,  24.U,  false.B, "bltu"),
    (  true.B, false.B, false.B, 13.U,    9.U,   "b111".U,  20.U ,  16.U,  36.U,  true.B,  "bgeu")
  )

  for (t <- tests) {
    poke(ctl.io.branch, t._1)
    poke(ctl.io.jal,    t._2)
    poke(ctl.io.jalr,   t._3)
    poke(ctl.io.inputx, t._4)
    poke(ctl.io.inputy, t._5)
    poke(ctl.io.funct3, t._6)
    poke(ctl.io.pc,     t._7)
    poke(ctl.io.imm,    t._8)
    step(1)
    expect(ctl.io.nextpc, t._9,  s"${t._11} wrong")
    expect(ctl.io.taken,  t._10, s"${t._11} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.NextPCBranchTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.NextPCBranchTesterLab2'
  * }}}
  */

class NextPCBranchTesterLab2 extends ChiselFlatSpec {
  "NextPC" should s"match expectations for each intruction type" in {
    Driver(() => new NextPC) {
      c => new NextPCBrTester(c)
    } should be (true)
  }
}


class NextPCJalTester(c: NextPC) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // branch,   jal,    jalr,   inputx,  inputy, funct3,    pc,    imm,   nextpc, taken
	  (  false.B, true.B, false.B,  13.U,    9.U,   "b000".U,  60.U ,  16.U,  76.U,  true.B, "jal0"),
    (  false.B, true.B, false.B,  133.U,   133.U, "b000".U,  40.U ,  8.U,   48.U,  true.B, "jal1")
  )

  for (t <- tests) {
    poke(ctl.io.branch, t._1)
    poke(ctl.io.jal,    t._2)
    poke(ctl.io.jalr,   t._3)
    poke(ctl.io.inputx, t._4)
    poke(ctl.io.inputy, t._5)
    poke(ctl.io.funct3, t._6)
    poke(ctl.io.pc,     t._7)
    poke(ctl.io.imm,    t._8)
    step(1)
    expect(ctl.io.nextpc, t._9,  s"${t._11} wrong")
    expect(ctl.io.taken,  t._10, s"${t._11} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.NextPCJalTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.NextPCJalTesterLab2'
  * }}}
  */

class NextPCJalTesterLab2 extends ChiselFlatSpec {
  "NextPC" should s"match expectations for each intruction type" in {
    Driver(() => new NextPC) {
      c => new NextPCJalTester(c)
    } should be (true)
  }
}



class NextPCJalrTester(c: NextPC) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // branch,   jal,    jalr,   inputx,  inputy, funct3,  pc,    imm,   nextpc, taken
	  (  false.B, false.B, true.B,  44.U,    99.U,  0.U,    100.U , 16.U,  60.U,   true.B, "jalr0"),
    (  false.B, false.B, true.B,  112.U,   19.U,  0.U,    56.U ,  12.U,  124.U,  true.B, "jalr1")
  )

  for (t <- tests) {
    poke(ctl.io.branch, t._1)
    poke(ctl.io.jal,    t._2)
    poke(ctl.io.jalr,   t._3)
    poke(ctl.io.inputx, t._4)
    poke(ctl.io.inputy, t._5)
    poke(ctl.io.funct3, t._6)
    poke(ctl.io.pc,     t._7)
    poke(ctl.io.imm,    t._8)
    step(1)
    expect(ctl.io.nextpc, t._9,  s"${t._11} wrong")
    expect(ctl.io.taken,  t._10, s"${t._11} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.NextPCJalrTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.NextPCJalrTesterLab2'
  * }}}
  */

class NextPCJalrTesterLab2 extends ChiselFlatSpec {
  "NextPC" should s"match expectations for each intruction type" in {
    Driver(() => new NextPC) {
      c => new NextPCJalrTester(c)
    } should be (true)
  }
}


class NextPCTester(c: NextPC) extends PeekPokeTester(c) {
  private val ctl = c

  val tests = List(
    // branch,   jal,    jalr,   inputx, inputy, funct3,      pc,    imm,   nextpc,  taken
    (  false.B, false.B, false.B, 143.U,   92.U,  "b000".U,  200.U, 164.U, 204.U,  false.B, "none0"),
	  (  true.B,  false.B, false.B, 13.U,    9.U,   "b000".U,  20.U,  16.U,  24.U,   false.B, "beqF"),
    (  true.B,  false.B, false.B, 133.U,   133.U, "b000".U,  28.U,  40.U,  68.U,   true.B,  "beqT"),
    (  true.B,  false.B, false.B, 11.U,    7.U,   "b001".U,  36.U,  12.U,  48.U,   true.B,  "bneT"),
    (  true.B,  false.B, false.B, 14.U,    14.U,  "b001".U,  52.U,  8.U,   56.U,   false.B, "bneF"),
    (  true.B,  false.B, false.B, 13.U,    9.U,   "b100".U,  24.U,  20.U,  28.U,   false.B, "bltF"),
    (  true.B,  false.B, false.B, 5.U,     7.U,   "b100".U,  12.U,  8.U,   20.U,   true.B,  "bltT"),
    (  true.B,  false.B, false.B, 130.U,   130.U, "b101".U,  24.U,  16.U,  40.U,   true.B,  "bgeT"),
    (  true.B,  false.B, false.B, 13.U,    94.U,  "b101".U,  28.U,  16.U,  32.U,   false.B, "bgeF"),
    (  true.B,  false.B, false.B, 13.U,    9.U,   "b110".U,  20.U,  16.U,  24.U,   false.B, "bltuF"),
    (  true.B,  false.B, false.B, 4.U,     8.U,   "b110".U,  4.U,   24.U,  28.U,   true.B,  "bltuT"),
    (  false.B, false.B, false.B, 151.U,   55.U,  "b000".U,  164.U, 12.U,  168.U,  false.B, "none1"),
    (  true.B,  false.B, false.B, 13.U,    9.U,   "b111".U,  20.U,  16.U,  36.U,   true.B,  "bgeuT"),
    (  true.B,  false.B, false.B, 11.U,    117.U, "b111".U,  68.U,  16.U,  72.U,   false.B, "bgeuF"),
    (  false.B, true.B,  false.B, 13.U,    9.U,   "b000".U,  204.U, 16.U,  220.U,  true.B,  "jal0"),
    (  false.B, true.B,  false.B, 133.U,   133.U, "b000".U,  208.U, 8.U,   216.U,  true.B,  "jal1"),
    (  false.B, false.B, true.B,  100.U,   919.U, "b000".U,  100.U, 16.U,  116.U,  true.B,  "jalr0"),
    (  false.B, false.B, true.B,  116.U,   119.U, "b000".U,  56.U,  12.U,  128.U,  true.B,  "jalr1")
  )

  for (t <- tests) {
    poke(ctl.io.branch, t._1)
    poke(ctl.io.jal,    t._2)
    poke(ctl.io.jalr,   t._3)
    poke(ctl.io.inputx, t._4)
    poke(ctl.io.inputy, t._5)
    poke(ctl.io.funct3, t._6)
    poke(ctl.io.pc,     t._7)
    poke(ctl.io.imm,    t._8)
    step(1)
    expect(ctl.io.nextpc, t._9,  s"${t._11} wrong")
    expect(ctl.io.taken,  t._10, s"${t._11} wrong")
  }
}

/**
  * This is a trivial example of how to run this Specification
  * From within sbt use:
  * {{{
  * Lab2 / testOnly dinocpu.NextPCTesterLab2
  * }}}
  * From a terminal shell use:
  * {{{
  * sbt 'Lab2 / testOnly dinocpu.NextPCTesterLab2'
  * }}}
  */

class NextPCTesterLab2 extends ChiselFlatSpec {
  "NextPC" should s"match expectations for each intruction type" in {
    Driver(() => new NextPC) {
      c => new NextPCTester(c)
    } should be (true)
  }
}
