---
title: 1. "GADT (Generalized Abstract Data Types) Basics"
date: 2018-07-31
draft: false
---

## Algebraic Data Type (ADT)

Algebraic Data type (ADT) is a type (or class) for objects whose behavior is defined by a set of value and a set of operations.
The definition of ADT only mentions what operations are to be performed but not how these operations will be implemented. [Citation](https://www.geeksforgeeks.org/abstract-data-types/)

In scala, this is just a sum type:
```tut:silent
sealed trait Expr
case class I(i: Int) extends Expr
case class B(b: Boolean) extends Expr
case class Add(a: Expr, b: Expr) extends Expr
case class Mul(a: Expr, b: Expr) extends Expr
case class Eq(a: Expr, b: Expr) extends Expr
```

## Generalized Algebraic Datatypes (GADTs)
Generalized algebraic datatypes (GADTs) are very similar to ADTs, however they also include a type parameter in the signature of the trait.
The addition of the type parameter allows the GADT to track the type of the expression, without referencing it within the class.
(Note that in Scala, Free Monad Algebras are coded as GADTs.)

```tut:silent
trait Expr[A]
case class I(i: Int) extends Expr[Int]
case class B(b: Boolean) extends Expr[Boolean]
case class Add(a: Expr[Int], b: Expr[Int]) extends Expr[Int]
case class Mul(a: Expr[Int], b: Expr[Int]) extends Expr[Int]
case class Eq(a: Expr[Boolean], b: Expr[Boolean]) extends Expr[Boolean]
```
An algebra is now defined that can used to build up programs without executing them.  Essentually, we are describing the program.
```tut
val intProgram = Add(I(5), Mul(I(3),I(2)))
val boolProgram = Eq(B(true), B(true))
```

If the types do not match up, the compilation will fail as expected.
```tut:fail
Add(I(5), Mul(I(3),B(true)))
```

We can then create an obvious interpreter which will execute the program description.
```tut:silent
def eval[A](expr: Expr[A]): A =
  expr match {
    case I(i) => i
    case B(b) => b
    case Add(a,b) => eval(a) + eval(b)
    case Mul(a,b) => eval(a) + eval(b)
    case Eq(a,b) => eval(a) == eval(b)
  }
```

```tut
eval(intProgram)
eval(boolProgram)
```

We can also create an interpreter which describes the program.
```tut:silent
def describe(expr: Expr[_]) : String =
  expr match {
    case I(i) => s"Integer: $i"
    case B(b) => s"Boolean: $b"
    case Add(a,b) => s"Add( ${describe(a)} and ${describe(b)})"
    case Mul(a,b) => s"Multiply( ${describe(a)} and ${describe(b)})"
    case Eq(a,b) => s"Compare( ${describe(a)} and ${describe(b)})"
  }
```

```tut
describe(intProgram)
describe(boolProgram)
```

And finally, the algabra can be converted to a different algebra as well.  In this case the type changes from Expr[A] to Description[A].
This conversion is also known as a natural transformation.
```tut:silent
case class Description[A](desc: String, expr: Expr[A])
def describe2[A](expr: Expr[A]): Description[A] =
  expr match {
    case exp@I(i) => Description[A](s"Integer: $i", exp)
    case exp@B(b) => Description[A](s"Boolean: $b", exp)
    case exp@Add(a,b) => Description[A](s"Add( ${describe(a)} and ${describe(b)}", exp)
    case exp@Mul(a,b) => Description[A]( s"Muliply( ${describe(a)} and ${describe(b)}", exp)
    case exp@Eq(a,b) => Description[A](s"Compare( ${describe(a)} and ${describe(b)})", exp)
  }
```
```tut
describe2(intProgram)
describe2(boolProgram)
```

## Recap
In this post, we've described what a GADT is and how it is different from an ADT.  We've shown how a GADT can be interpretered or go through a natural transformation.

This page is a port of the (Haskell GADT Wiki Page)[https://en.wikibooks.org/wiki/Haskell/GADT]
