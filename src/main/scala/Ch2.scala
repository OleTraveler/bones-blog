object Ch2 {

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

  val program = KvpPair(KvpInt("id"), KvpString("name"))

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

  // print the result
  docResult

//  Our second interpreter, the JSON extraction interpreter, will extract the
//  key value pair form a JSON blob based on the original program description.


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
  // Finally print the result



//  So now lets create a second program description and some input.

  val program2 =
    KvpPair(
      KvpPair(KvpInt("id"), KvpString("firstName")),
      KvpPair(KvpString("lastName"), KvpBool("voted"))
    )

  val input2 = Parse.parse(""" { "id": 5, "firstName": "Kowey", "lastName": "Duploid", "voted": true } """).right.get
//  Now we will run the definition through the same interpreters.
  eval(program2).apply(input2)

  doc(program2)

}
