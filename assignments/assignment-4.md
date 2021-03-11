---
Author: Jason Lowe-Power
Editor: Maryam Babaie
Title: DINO CPU Assignment 4
---

# DINO CPU Assignment 4: Branch Predictor and Benchmarking

Originally from ECS 154B Lab 4, Winter 2019.

Modified for ECS 154B Lab 4, Winter 2021.

**Due on 02/28/2021.**

# Table of Contents

* [Introduction](#introduction)
    * [Pipeline design constraint](#pipeline-design-constraint)
    * [Updating the DINO CPU code](#updating-the-dino-cpu-code)
    * [Goals](#goals)
* [Pipelined CPU design](#pipelined-cpu-design)
    * [Running simulations](#running-simulations)
    * [CPU designs and branch predictors](#cpu-designs-and-branch-predictors)
* [What has been changed](#what-has-been-changed)
    * [`BaseBranchPredictor` details](#basebranchpredictor-details)
    * [Local history predictor](#local-history-predictor)
        * [Testing the local history predictor](#testing-the-local-history-predictor)
    * [Global history predictor](#global-history-predictor)
        * [Testing the global history predictor](#testing-the-global-history-predictor)
* [Part I: Adding the branch predictor to the pipelined CPU](#part-i-adding-the-branch-predictor-to-the-pipelined-CPU)
* [Part II: Running experiments](#part-ii-running-experiments)
    * [Single cycle vs. pipelined](#single-cycle-vs-pipelined)
    * [Comparison of branch predictors](#comparison-of-branch-predictors)
    * [Area overhead of branch predictors](#area-overhead-of-branch-predictors)
* [Grading](#grading)
* [Submission](#submission)
    * [Code portion](#code-portion)
    * [Written portion](#written-portion)
    * [Academic misconduct reminder](#academic-misconduct-reminder)
* [Hints](#hints)


# Introduction

![Cute Dino](../dino-128.png)

In the last assignment, you implemented a pipelined RISC-V CPU.
You implemented forwarding to reduce the impact of data hazards, but control hazards still caused significant performance degradation.
At least, that was your assumption.

In this assignment, you will be extending this design with different branch predictor implementations and evaluating the performance of your pipelined design to try to improve its performance.
You will add a local history predictor and a global history predictor.
Then, you will compare their performance.

## Pipeline design constraint

For this assignment, you must use the template code as a baseline.
If you use your own pipeline as the basis instead of the template code, **you may get the wrong results.**

## Updating the DINO CPU code

The DINO CPU code must be updated before you can run each lab.
You should read up on [how to update your code](../documentation/updating-from-git.md) to get the assignment 4 template from GitHub.

You can check out the main branch to get the template code for this lab.

## Goals

- Learn how to incorporate different branch predictor designs in a pipelined CPU.
- Evaluate different CPU designs.
- Evaluate trade-offs between different designs.

# Pipelined CPU design

Below is an updated design for the DINO CPU pipeline with a branch predictor. **The new parts have been sketched in red**.
There are some main differences, which are highlighted.

1. There is now a branch predictor unit added in the decode stage.
2. There is a branch adder added to the decode stage.
3. There is a unified MUX at the fetch stage. Compared to the previous design, it has one more option for the branch destination calculated in ID stage.
4. There is a new logic (red box) to handle the `taken`and `nextpc` for EX/MEM pipereg, `bp-update` and `bp-taken` for branch predictor unit at the execute stage, whenever needed (e.g. in case of a branch instruction).
5. The hazard unit has been slighty extended to support branch prediction and deal with taken branches in the ID stage and branch mispredictions in execute stage.

You are required to implement the design on the DINO CPU source code **without adding any aditional module**.

![Pipelined CPU](../documentation/pipelined-bp.svg)

**BE SURE TO PULL THE LATEST CODE FROM GITHUB!**
If you don't, you may get the wrong results below.
See [Updating the DINO CPU code](#updating-the-dino-cpu-code) above.

## Running simulations

In this assignment, you will be running a number of simulations to measure the performance of your CPU designs.
Some of these simulations may run for millions of cycles.
They may take a few minutes on the lab computers, and possibly longer on your laptops, especially if you are using Vagrant or virtualization.
All of the tests run in less than 30 seconds on my desktop.

To run experiments, you are going to use the `simulate` main function.
The code can be found in [`simulate.scala`](../src/main/scala/simulate.scala).
This main function takes two parameters: the binary to run, and the CPU design to create.

```scala
sbt:dinocpu> runMain dinocpu.simulate <test name> <cpu name> <branchPredictor name>
```

### Test names

For the `test name`, you will use the names below.

Binaries:
- `median.riscv`: performs a 1D three element median filter
- `multiply.riscv`: tests the software multiply implementation
- `qsort.riscv`: quick sort
- `rsort.riscv`: radix sort
- `towers.riscv`: simulation of the solution to the [Towers of Hanoi](https://en.wikipedia.org/wiki/Tower_of_Hanoi)
- `vvadd.riscv`: vector-vector add

You can find binaries for the six benchmarks in the [`/src/test/resources/c`](../src/test/resources/c) directory.
The source is also included in the subdirectories.

### CPU designs and branch predictors

You will be evaluating five CPU designs: the single cycle from assignment 2, and the pipelined design from assignment 3 which will be extended to support branch predictor unit with four different branch predictors in Part I of this assignment and it's called pipelined-bp CPU.

- `single-cycle`: The single cycle CPU design.
- `pipelined-bp`: The pipelined CPU design with branch predictor.

After the word `pipelined-bp`, you can specify the branch predictor type.
For instance, for "always taken" you would say: `pipelined-bp always-taken`.

Therefore, you will be running the following CPU types:

- `single-cycle`: The single cycle CPU design.
- `pipelined-bp always-not-taken`: The pipelined CPU design from assignment 3.
- `pipelined-bp always-taken`: The pipelined CPU design with an always taken branch predictor
- `pipelined-bp local`: The pipelined CPU design with a local history predictor.
- `pipelined-bp global`: The pipelined CPU design with a global history predictor.

Note: the simulator will time out after 3 million cycles.
Even with a 10 cycle latency, no workload will take more than 3 million cycles with this design.


In order to answer the questions below, you will need to study this code and understand what these algorithms are doing.

You can also use `sigularity exec` instead of the `sbt` REPL.

As an example, here's the output when running a simulation for `median` with pipelined-bp CPU and local branch predictor.

``` scala
sbt:dinocpu> runMain dinocpu.simulate median.riscv pipelined-bp local
[info] Running dinocpu.simulate median.riscv pipelined-bp local
Running test median.riscv with memory latency of pipelined-bp cycles
[info] [0.001] Elaborating design...
CPU Type: pipelined-bp
Branch predictor: local
Memory file: test_run_dir/pipelined-bp/median.riscv/median.riscv.hex
Memory type: combinational
Memory port type: combinational-port
Memory latency (ignored if combinational): 0
[info] [1.241] Done elaborating.
Total FIRRTL Compile Time: 1206.1 ms
file loaded in 0.187830544 seconds, 1255 symbols, 1219 statements
Running for max of 3000000
0 cycles simulated.
Finished after 9326 cycles
Test passed!
[success] Total time: 7 s, completed Feb 15, 2021 12:31:02 AM
```


As shown above, the simulator prints some useful statistics.
Specifically, it prints the total number of cycles taken to run the workload (9326 in this case), also prints details about the system that it is simulating (elaborating) before the simulation begins, and that the application has passed the test successfully.

Note: You will use this output to answer a number of questions below.


Note: Some tests require millions of cycles.
This can take a significant amount of time, especially if you are using a virtualized environment (e.g., vagrant).
On my machine (Intel(R) Core(TM) i7-7700 CPU @ 3.60GHz) all of the tests took about 25 minutes to execute.
The lab machines (e.g., pc01, etc.) should take about the same amount of time.
However, if you use a virtualized environment, I would expect a 2x slowdown or more.

# What has been changed

For this assignment, you will be writing less Chisel code than previous assignments.
Thus, all of the code will be done in one part.

In this assignment, you will be modifying the `hazard-bp.scala` file in `src/main/scala/components` and `cpu-bp.scala` in `src/main/scala/pipelined`.
The template code provided already has the base code for pipelined CPU design in assignment 3. The branch predictor unit is already implemented and you are required to complete the hazard-bp unit and extend the pipelined CPU, to incorporate the branch predictor into your CPU.

The branch predictor unit is located in `src/main/scala/components/branchpred.scala` file. Inside of this file, there is a `BaseBranchPredictor` which has some convenience functions to allow for a very flexible branch predictor implementation.
Chisel allows for *parameterized* hardware, which, until now, we have not taken advantage of.
`configuration.scala` has parameters for the size of the branch prediction table and the number of bits for the table's saturating counters.
The template code handles all of the parameterized logic for you. The modules `LocalPredictor` and `GlobalHistoryPredictor`, `AlwaysTakenPredictor` and `AlwaysNotTakenPredictor` have been implemented for you.
You simply can instantiate a branch predictor unit in your `cpu-bp.scala` file and connect it to the other modules in your pipelined design and apply other modifications as needed.

In the next section we will explain the details of branch predictor used in this assignment.

## `BaseBranchPredictor` details

The base branch predictor instantiates a set of registers to hold the prediction table (`predictionTable`).
It's been used in local and global predictors to store the predictions for future branches based on past history.
Here's a few examples on how to use the table.

To get the current value out of the table for a particular index, you can use the following:

```scala
val index = Wire(UInt(tableIndexBits.W))
val value = predictionTable(index)
```

Note that `tableIndexBits` is the number of bits needed to index the table, `log_2(number of table entries)`.

Additionally, the `BaseBranchPredictor` has two functions to increment and decrement saturating counters.
You can pass a Chisel register to these functions to increment/decrement the value and store it back to the same location.
For instance, if you wanted to decrement a saturating counter in the branch history table and store it back to the same location, you could use the following:

```scala
decrCounter(predictionTable(index))
```

`incrCounter` will increment a saturating counter.
See the code in `BaseBranchPredictor` for details.

## Local history predictor

For this predictor, the PC of the branch is used to **predict** whether the branch is taken or not taken.
The figure below shows the high-level function of the local branch predictor.

![Local history branch predictor](local-predictor.svg)

The prediction has been implemented such that every cycle given the incoming PC the branch predictor will predict either taken or not taken for that PC.
Second, whenever the `io.update` input is high, the prediction for the *last* PC that was predicted is needed to be updated.
The prediction is updated based on the `io.taken` input (if true, the branch was taken, if false it was not taken).

*Hint on getting the 'last' PC*: the predictor is always updated one cycle after the prediction is made.

### Testing the local history predictor

To test the local history predictor, you can use the following tests:

```scala
sbt:dinocpu> testOnly dinocpu.LocalPredictorUnitTesterLab4
```
Note: This part is already implemented for you and it will successfully pass the test.

## Global history predictor

Instead of using the PC to predict if a branch is taken or not, the global predictor uses the last `N` branches.
For instance, if the last `N` branches were `TNTNTN`, you might predict the next branch would be taken.

![Global history branch predictor](global-predictor.svg)

Thus, it's needed to keep track of the history of the last `N` branches.
Then, this history can be used to index into the prediction table as shown below.

To implement this, first the history shift register has been implemented which is updated every time `io.update` is true (since this is when branches are known).
Then, the history register is used to make a prediction on every cycle.
Finally, the history register is used to update a particular entry in the prediction table every time a branch is executed and it is known whether it was taken or not (i.e., when `io.update` is true).

### Testing the global history predictor

To test the local history predictor, you can use the following tests:

```scala
sbt:dinocpu> testOnly dinocpu.GlobalPredictorUnitTesterLab4
```

Note: This part is already implemented for you and it will successfully pass the test.

# Part I: Adding the branch predictor to the pipelined CPU
As explained in the [previous sections](#pipelined-cpu-design), we have a single MUX at the fetch stage, there are two additional units at the decode stage, a branch adder and branch predictor unit, and the `hazard-bp.scala`, and `cpu-bp.scala` are required to be modified so the entire branch prediction process can be carried out throughout the pipelined CPU. We'll go over the details of what's needed be implemented in this assignment again.

1. There is a unified MUX at the fetch stage. Compared to the previous design, it has one more option for the branch destination calculated by the branch adder in the decode stage.
2. The hazard unit has been slightly extended to support branch prediction and deal with taken branches in the ID stage. The unit has one output to select the proper value for pc, `pcSel`. It also receives the prediction by the branch predictor to assign the proper value to the pc at fetch stage.
3. There is now a branch predictor unit added in the decode stage. See the [previous section](#basebranchpredictor-details) for more details.
4. There is a branch adder added to the decode stage which calculates the destination of the predicted branch.
5. At the execute stage, there is a red box which is basically an abstracted logic to handle the final value for`taken` and `nextpc` calculated by the nextpc module before putting them into the EX/MEM pipereg. This is particularly required for branch instructions, to compare the actual outcome of the branch calculated in execute stage with what has been predicted by branch predictor at the decode stage, as well as to update the branch predictor.

## **Hints & Important Notes**

1. The `nextpc` calculated by the nextpc module, can be directly fed to the EX/MEM pipereg, for both jump and branch instructions.
2. The `taken` calculated by the nextpc module, can be directly fed to the EX/MEM pipereg, *only* for jump instructions.
3. In case of a branch instruction at the execute stage, the `taken` in the *EX/MEM pipereg*, should be used as a signal to show whether the prediction by the branch predictor has been correct or not, rather than the actual outcome of the branch calculated by the nextpc module which is what you did in the assignment 3. The piece of logic you put in the red box, should handle this. So, pay attention that you should not directly connect the output `taken` of the nextpc module to the `taken` in the *EX/MEM pipereg*, otherwise your hazard-bp unit will not work properly.
4. You can consider the signals entered to the left side of the red box as required inputs to implement the logic, and the signals came out of the right side of the box as the outputs. These signals are sufficient to fully implement the logic and cover all the corner cases.

## Testing

You can use each of the following commands to test your implementation for pipelined-bp CPU.

To test for pipelined-bp CPU with `always-not-taken` branch predictor:
```scala
sbt:dinocpu> testOnly dinocpu.SmallApplicationsNotTakenTesterLab4
sbt:dinocpu> testOnly dinocpu.LargeApplicationsNotTakenTesterLab4
```

To test for pipelined-bp CPU with `always-taken` branch predictor:
```scala
sbt:dinocpu> testOnly dinocpu.SmallApplicationsTakenTesterLab4
sbt:dinocpu> testOnly dinocpu.LargeApplicationsTakenTesterLab4
```

To test for pipelined-bp CPU with `local` branch predictor:
```scala
sbt:dinocpu> testOnly dinocpu.SmallApplicationsLocalTesterLab4
sbt:dinocpu> testOnly dinocpu.LargeApplicationsLocalTesterLab4
```

To test for pipelined-bp CPU with `global` branch predictor:
```scala
sbt:dinocpu> testOnly dinocpu.SmallApplicationsGlobalTesterLab4
sbt:dinocpu> testOnly dinocpu.LargeApplicationsGlobalTesterLab4
```

Finally, you can run all the tests designed for this assignment by the following command.

```scala
sbt:dinocpu> Lab4 / test
```

You can also use single-step for debugging your CPU for any of the previous or new test cases. Below is a couple of example commands to run single-step for this assignment.

```scala
sbt:dinocpu> runMain dinocpu.singlestep beq-True pipelined-bp global

sbt:dinocpu> runMain dinocpu.singlestep qsort.riscv pipelined-bp local

sbt:dinocpu> runMain dinocpu.singlestep fibonacci pipelined-bp always-taken
```

**Important Notice 1**

Your implementation of pipelined-bp CPU will affect the number of cycles it takes to execute a program (depending on how many bubbles/stalls are added by the CPU when you're implementing the hazard-bp unit). In order to unify different designs, we have given you the number of cycles which our pipelined-bp CPU has taken for running **`vvadd.riscv`** for four different branch predictors which is shown below. This will give you an estimate about the number of cycles your CPU should take.

| Branch Predictor | Finished after #cycles |
|------------------|------------------------|
| always-not-taken |        17569           |
| always-taken     |        17579           |
| local            |        14587           |
| global           |        14599           |



**Important Notice 2**

Part I has 20% of the final grade for this assignment and **there's no partial credit** if your implementation passes only some of the test cases and fails for the others.


**Important Notice 3**

As we do not provide partial credit for Part I of this assignment, we'll provide the full implementation of the pipelined-bp CPU immediately after the due date (02/28/2021 11:59pm), so in case you were not able to implement the pipelined-bp CPU you'll lose 20% and still can work on Part II and get credit for the second part of the assignment. Please submit your pipelined-bp implementation code on time, as any **late** submission for the code portion of this assignment (Part I), will have 0 credit for that part.

# Part II: Running experiments

The bulk of this assignment will be running experiments and answering questions.
Once you have correct implementation of pipelined-bp CPU, you can start trying to decide how to design the best CPU!

The workloads are the six benchmark binaries [mentioned above](#running-simulations).
Make the following assumptions for the questions below:

| CPU Design   | Frequency |
|--------------|-----------|
| Single cycle | 1 GHz     |
| Pipelined    | 3 GHz     |

Feel free to answer questions in prose, as a table, or as a graph.
However, make sure your answers are **legible**!
These questions *will be graded*.
We know the correct answers since everyone is using the same pipeline design.

I strongly suggest using graphs and writing your answers using a word processor.
I suggest you *do not* write your answers by hand.

## Single cycle vs. pipelined

In this part, you will run different CPU designs (single-cycle and pipleined-bp) and compare their performance.

1. For each workload, what is the total number of instructions executed?
2. For each workload, what is the CPI for the pipelined CPU with an *always not taken* branch predictor?
3. Given the frequency assumptions above, what is the speedup of the pipelined design with the always not taken branch predictor over the single cycle design for each workload?

## Comparison of branch predictors

In this part, you will run the benchmarks with the new branch predictors you designed, compare their performance, and explain why you see that performance.

4. For each workload, what is the best performing branch predictor?
5. What is the speedup of the best performing branch predictor compared to *always not taken* for each workload?
6. Compare the workloads for which the global history predictor does better than the local history predictor. Look at the C code given in the `src/test/resources/c/` directory. Explain why the global history predictor does better than the local history predictor for one of these workloads.

## Area overhead of branch predictors

In this section, you will compare the performance and cost of different branch predictor designs.
You are trying to maximize the area-performance trade-off.
You can modify the size of the branch prediction table by changing the variables `saturatingCounterBits` and `branchPredTableEntries` in `src/main/scala/configuration.scala` (lines 25 and 27).

Assume the following.
Note: these are made-up numbers.
Don't use this in your research or job in industry.

| Design              | Area     |
|---------------------|----------|
| No branch predictor | 1 mm^2   |
| 1 Kilobyte of SRAM  | 0.1 mm^2 |

7. What is the size (in bytes, B) of the default branch predictor with 2 bits per saturating counter and 32 entries?
8. For each workload, what is the performance improvement if you increase the size of the branch predictor to 256 entries for the local predictor?
9. For each workload, what is the performance improvement if you keep the number of predictor entries at 32 and increase the saturating counter bits to 3 for the local predictor? What about increasing the saturating counter bits to 8? Explain why you see a speedup for 3 bits and a slowdown for 8 bits **for qsort**.
10. At a high level (e.g., you don't have to show the data), compare the percent area overhead of the previous designs (with 256 entries and 2-bit counters, with 32 entries and 3-bit counters, and with 32 entries and 8-bit counters) to the performance improvement over the baseline design with 32 entries and 2-bit counters. Do you believe the increased area is worth it? Be sure to talk about the characteristics of the benchmark code in your answer.


# Grading

Grading will be done on Gradescope.
See [the Submission section](#Submission) for more information on how to submit to Gradescope.

| Name         | Percentage |
|--------------|------------|
| Part I       | 20%        |
| Part II      | 80%        |

# Submission

**Warning**: read the submission instructions carefully.
Failure to adhere to the instructions will result in a loss of points.

## Code portion

You will upload the files that you changed (`src/main/scala/components/hazard-bp.scala` and `src/main/scala/pipelined/cpu-bp.scala`) to Gradescope on the `Assignment 4: Code` assignment.

Once uploaded, Gradescope will automatically download and run your code.
This should take less than 5 minutes.
For each part of the assignment, you will receive a grade.
If all of your tests are passing locally, they should also pass on Gradescope unless you made changes to the I/O, **which you are not allowed to do**.

Note: There is no partial credit on Part I and it is all or nothing.

## Written portion

You will upload your answers for the `Assignment 4: Written` assignment to Gradescope.
**Please upload a separate page for each answer!**
Additionally, I believe Gradescope allows you to circle the area with your final answer.
Make sure to do this!

We will not grade any questions for which we cannot read.
Be sure to check your submission to make sure it's legible, right-side-up, etc.

## Academic misconduct reminder

You are to work on this project **individually**.
You may discuss *high level concepts* with one another (e.g., talking about the diagram), but all work must be completed on your own.

**Remember, DO NOT POST YOUR CODE PUBLICLY ON GITHUB!**
Any code found on GitHub that is not the base template you are given will be reported to SJA.
If you want to sidestep this problem entirely, don't create a public fork and instead create a private repository to store your work.
GitHub now allows everybody to create unlimited private repositories for up to three collaborators, and you shouldn't have *any* collaborators for your code in this class.

# Hints

- **Start early!** Start early and ask questions on Discord and in discussions.
- If you need help, come to office hours for the TA, or post your questions on Discord.
- See [common errors](../documentation/common-errors.md) for some common errors and their solutions.
