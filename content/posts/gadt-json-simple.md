---
title: 2. GADT Simple Json Extractor
draft: false
---

# Overview

In this post we are going to create a simple GADT Algebra with which 
will describe how to extract data from a JSON blob into
validated structured data. This post will be a very high
level overview describing a particular use of GADTs.  We will dive deeper into what
is happening in later posts. 

## Algebra

The goal fo the algebra is to be able to provide a description of what a program can do.

```scala
  // This is the algebra, Kvp (Key Value Pair) will be our base trait.
  sealed abstract class Kvp[A]

  // Below, the key property will be the string we 
  //  look for in a JSON blob and Kvp[String]
  //  implies we are extracting a String.  We will also
  //  define behavior to extract an Int, Boolean and a Pair.
  case class KvpString(key: String) extends Kvp[String]
  case class KvpInt(key: String) extends Kvp[Int]
  case class KvpBool(key: String) extends Kvp[Boolean]
  case class KvpPair[A,B](p1: Kvp[A], p2: Kvp[B]) extends Kvp[(A,B)]
```

## Program

Here is our program.  We are defining that we are expecting a
key with name 'id' which should be an int and also a key with the name
'name' which should be a string.  We combine the result into a Pair.

```scala
  val program = KvpPair(KvpInt("id"), KvpString("name"))
```

## Doc interpreter

Our doc interpreter will interpret the data structure of our Program and print out
what type of data structure we are expecting.
```scala
  // An interpreter will print out what the program does.
  def doc(kvp: Kvp[_]): String =
    kvp match {
      case KvpString(key) => s"Expecting a String with key ${key}"
      case KvpInt(key) => s"Expecting an Int with key ${key}"
      case KvpBool(key) => s"Expecting a Bool with key ${key}"
      case KvpPair(p1,p2) => doc(p1) + "\n" + doc(p2)
    }

  // Run the program through the interpreter
  val docResult = doc(program)
```

```scala
scala>   // print the result
     |   docResult
res8: String =
Expecting an Int with key id
Expecting a String with key name
```

## JSON extraction interpreter

Our second interpreter, the JSON extraction interpreter, will extract the
key value pair form a JSON blob based on the original program description.
 

```scala
   import argonaut._, Argonaut._
   
  def eval[A](kvp: Kvp[A]) : Json => A = {
    val result = kvp match {
      case KvpString(key) => 
        (json: Json) => 
          json.field(key).flatMap(_.string).get
      case KvpInt(key) => 
        (json: Json) => 
          json.field(key).flatMap(_.number).flatMap(_.toInt).get
      case KvpBool(key) =>
        (json: Json) =>
          json.field(key).flatMap(_.bool).get    
      case KvpPair(p1, p2) => 
        (json: Json) => 
          (eval(p1).apply(json), eval(p2).apply(json))
    }
    // This is an unforuntate consequence of using GADTs in Scala
    // If someone understand how to get rid of this case, please advise.
    result.asInstanceOf[Json => A]
  }

  // Here we are running the exact same program defined above through this interpreter. 
  //  The result is a program with type Json => (Int, String)
  val runtime = eval(program)

  // The simple input will pass to the runtime
  val input = 
    Parse.parse(""" { "id": 5, "name" : "Simon Peyton Jones" } """).right.get

  // Run the input through the runtime.
  val evalResult = runtime.apply(input)
```

```scala
scala>   // Finally print the result
     |   evalResult
res14: (Int, String) = (5,Simon Peyton Jones)
```


# A Second Program

So now lets create a second program description and some input.

```scala
  val program2 = 
    KvpPair(
        KvpPair(KvpInt("id"), KvpString("firstName")), 
        KvpPair(KvpString("lastName"), KvpBool("voted"))
    )
  
  val input2 = Parse.parse(""" { "id": 5, "firstName": "Kowey", "lastName": "Duploid", "voted": true } """).right.get
```

Now we will run the definition through the same interpreters.
```scala
scala>   eval(program2).apply(input2)
res15: ((Int, String), (String, Boolean)) = ((5,Kowey),(Duploid,true))

scala>   doc(program2)
res16: String =
Expecting an Int with key id
Expecting a String with key firstName
Expecting a String with key lastName
Expecting a Bool with key voted
```



# Conclusion

This post is mean to show an interesting application of what we can do with a GADT Algebras.  First we used the
Algebra to describe the program.  Next we demonstrated that the program description 
can be run through different interpreters to
produce different results.  In this post we showed how to extract data from a JSON blob and also 
print documentation on what data is expected.  

One of the most important principals to take away here is how we can run the different program description through
the same interpreter. If a program description changes, then behavior will change AND the documentation is updated.
We will continue to dive into this idea of having multiple interpreters acting upon the same program description.    


