/* Generated By:JavaCC: Do not edit this line. PCDVisitor.java Version 5.0 */
package org.biopcd.parser;

public interface PCDVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTparseFullMenu node, Object data);
  public Object visit(ASTparseMenuItem node, Object data);
  public Object visit(ASTBody node, Object data);
  public Object visit(ASTContent node, Object data);
  public Object visit(ASTTab node, Object data);
  public Object visit(ASTPanel node, Object data);
  public Object visit(ASTAct node, Object data);
  public Object visit(ASTParam node, Object data);
  public Object visit(ASTbuttonFields node, Object data);
  public Object visit(ASTlistFields node, Object data);
  public Object visit(ASTtextFields node, Object data);
  public Object visit(ASTtextAreaFields node, Object data);
  public Object visit(ASTnumberFields node, Object data);
  public Object visit(ASTdecimalFields node, Object data);
  public Object visit(ASTfileFields node, Object data);
  public Object visit(ASTdirFields node, Object data);
  public Object visit(ASTtempfileFields node, Object data);
  public Object visit(ASTFileFormat node, Object data);
  public Object visit(ASTSystemName node, Object data);
  public Object visit(ASTArchList node, Object data);
  public Object visit(ASTArchName node, Object data);
  public Object visit(ASTParseDBConnect node, Object data);
  public Object visit(ASTFullSQLQuery node, Object data);
  public Object visit(ASTIdent node, Object data);
  public Object visit(ASTText node, Object data);
  public Object visit(ASTDecimal node, Object data);
  public Object visit(ASTNumber node, Object data);
  public Object visit(ASTBool node, Object data);
}
/* JavaCC - OriginalChecksum=978256d51cc112ec191bd8b1cfac5f0a (do not edit this line) */
