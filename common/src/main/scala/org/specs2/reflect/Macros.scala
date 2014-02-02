package org.specs2
package reflect

import scala.reflect.macros.blackbox.Context

object Macros {

  def toAST[A](c: Context)(xs: c.Tree*)(implicit tt: c.TypeTag[A]): c.Tree = {
    import c.universe._
    Apply(Select(Ident(typeOf[A].typeSymbol.companionSymbol), newTermName("apply")), xs.toList)
  }

  def methodCall(c: Context)(name: String, xs: c.Tree*): c.Tree = {
    import c.universe._
    Apply(Ident(newTermName(name)), xs.toList)
  }

  def stringExpr(c: Context)(variable: c.Expr[Any]): c.Tree =
    c.literal(sourceOf(c)(variable)).tree

  def sourceOf(c: Context)(expr: c.Expr[_]): String = {
    val p = expr.tree.pos
    val source = new String(p.source.content)
    if (p.isRange) source.substring(p.start, p.end)
    else p.lineContent.substring(p.point - p.source.lineToOffset(p.source.offsetToLine(p.point)))
  }

  def termName(c: Context)(m: c.Expr[Any]): c.Expr[String] = {
    import c.universe._
    val name = m.tree match {
      case Ident(termName)                                       => termName
      case Select(_, termName)                                   => termName
      case Apply(Select(_, termName), _)                         => termName
      case Apply(Ident(termName), _)                             => termName
      case Apply(TypeApply(Ident(termName), _), _)               => termName
      case Apply(TypeApply(Select(_, termName), _), _)           => termName
      case Apply(Apply(TypeApply(Ident(termName), _), _), _)     => termName
      case Apply(Apply(TypeApply(Select(_, termName), _), _), _) => termName
      case Function(_, Apply(Select(_, termName), _))            => termName
      case other                                                 => c.abort(m.tree.pos, "The code must be a member selection, or a function application:\n"+showRaw(m.tree))
    }
    c.literal(name.toString.trim)
  }

}
