# Gridlock  
Gridlock is a Groovy based framework and DSL for creating load drivers. The Gridlock DSL is "just code" and allows the perfect mix of flexibility and structure by separating the test definition and action sequence from the action implementations.

## Why Gridlock?
Gridlock is a "developer's" load driver framework, which lends itself to certain strengths. Gridlock's actions are completely flexible.  Where other frameworks typically provide a set of first-order capabilities (make a REST call, execute a SQL query, etc...) and then provide a script or meta-language for more complex actions, Gridlock only executes code.  Where other frameworks constrain the inputs and outputs to each action, Gridlock only executes code.  The results is a action that behaves exactly as a developer has coded it to behave.

## Why Not Gridlock?
- Gridlock has no UI
- Gridlock provides no native libraries, other than Java and Groovy, to create meaningful actions

## Framework Philosophy
Gridlock is the result of a struggle to balance the structure of a declarative framework with the power of higher-order programming languages. A good example of this balance is Gradle which inspired the use of a Groovy DSL in Gridlock.

## Load Driver Philosophy
The goal of a load driver is to produce a workload for the SUT.  Typically the quantity of the workload is expressed in actions per minute or by some level of concurrency.

Gridlock is designed to allow the user to express the rate of actions and concurrency by controlling the scheduling of UseCase instance and the cadence of actions.  These two timing properties are decoupled so that test can be designed to tune concurrency and action rates separately.  Gridlock describes the rate at which it schedules new UseCase instances as **period** and expresses the cadence of actions through the **execution time**.

### Examples

#### Configuration A
&#35; of actions: 5 (A, B, C, D, E)
**period**: 2 seconds
**execution time** 10 seconds

```
uci: 1|A  B  C  D  E
     2|   A  B  C  D  E
     3|      A  B  C  D  E
     4|         A  B  C  D  E
     5|            A  B  C  D  E
     6|               A  B  C  D  E
     7| ...
time:  0  2  4  6  8  0  2  4  6  8
```
This result of Configuration A is that at any point after 8 seconds into the test there are 5 concurrent connections and 30 of each action per minute.

#### Configuration B
&#35; of actions: 5 (A, B, C, D, E)
**period**: 2 seconds
**execution time** 15 seconds

```
uci: 1|A   B   C   D   E     
     2|   A   B   C   D   E
     3|      A   B   C   D   E
     4|         A   B   C   D   E
     5|            A   B   C   D   E
     6|               A   B   C   D   E
     7|                  A   B   C   D   E
     8|                     A   B   C   D   E
     9|...
time:  0  2  4  6  8  0  2  4  6  8  0  2  4  6
```
The result of Configuration B achieves the same actions per minute of Configuration A but increases concurrency to ~7.5.

## Getting Started with TestBuilder
The TestBuilder class provides an entry point for the Gridlock DSL.

```
def testBuilder = new TestBuilder('RestAPITest')
```

The DSL is started with the **define** method:

```
testBuilder.define {...}
```

## Creating a UseCase Definition
A UseCase Definition is a blueprint for the sequence of actions necessary to execute a user story.  It also specifies *how often* the sequence should be run and the *cadence* of each action. It defines a *source* and *sink* for a context object called *"param"* that is passed to every action in the sequence.

To create and name a UseCase Definition:

```
useCase("example") { }
```

### Param Source and Sink:
A context object, hereafter called **param**, can be passed to each action in the chain.  
- **paramSource** (Closure) is executed to initialize the context object.  It's return value is used as **param** in actions.
- **paramSink** (Closure) is executed after all actions are completed or if there is a failure.

```
useCase("example") {
  paramSource = {
    return new MyContextObject()
  }

  paramSink = { myObj ->
    myObj.close()
  }
}
```

### Timing
The **timing** DSL controls the rate at which new use case instances are created and executed as well as the cadence of the use case's actions.  This is the primary mechanism to control the concurrency and rate of actions for use case instances in Gridlock.

- **periodFunction** (Closure<Long>) returns the number of milliseconds to wait before stating the next use case instance.
- **executionTime** is a number and time unit symbol corresponding to the amount of time an actions should be spread over.  The available unit symbols are: *hour*, *min*, *sec*, *msec*.

```
  useCase("example") {
    timing = {
      periodFunction = { return 30_000 }
      executionTime = 20 sec
    }    
  }
```

### Failures
For long running tests it may be desireable to tolerate a certain number of failures.  Use the **numAllowedFailures** property to set the number of tolerated failures (default 0).

```
useCase("example") {
  numAllowedFailures = 10
}
```

### Action Flow
The **actionFlow** DSL defines the order in which actions are executed.  You can specify a multiplier to execute an action multiple times.  Actions can also be nested within each other to control order of operation.  Each action requires a name that matches (case sensitive) an action definition.

A simple action flow:
```
actionFlow {
  action(firstAction)
  action(name: secondAction)
  action(name: thirdActionFiveTimes, repetitions: 5)
}
```

This nesting example reads "run action A, for each action A run action B five times and for each action B run action C one time":
```
//A-B-C-B-C-B-C-B-C-B-C
actionFlow {
  action(A) {
    action(name: B, repetitions: 5) {
      action(C)
    }
  }
}
```

### Action Definitions
The **actionDef** DSL is used to implement each action in the action flow.  The DSL requires a **name** property matching an action name from the **actionFlow** DSL.  It also requires **args** and **returns** properties which control the inputs and outputs of the action.  The **executes** method specifies the closure executed as the "work" of the action.

- **name** must match an action name from the **actionFlow** DSL
- **args** is a list of arguments passed to the **executes** closure.  To specify results from a previous action use the **results()** function.
- **returns** is a list of tokens which are matched against the map keys returned by the **executes** closure
- **executes** is a closure with arguments provided by **args** and returns a map with keys matching the tokens in **returns**

```
//basic action
actionDef("A") {
  args = []
  executes {
    println "action A doing some work..."
  }
  returns = []
}

//action with input and output
def str = "desserts"
actionDef("reverse") {
  args = [str]
  executes { s ->
    def strOut = s.reverse()
    return ['gnirts': strOut]
  }
  returns = [gnirts]
}

//action using previous actions output
actionDef("upper") {
  args = [result(gnirts)]
  executes { s -> 
    assert s.equals("stressed")
    def strOut = s.toUpper()
    return ['STRING': strOut]
  }
  returns = [STRING]
}
```