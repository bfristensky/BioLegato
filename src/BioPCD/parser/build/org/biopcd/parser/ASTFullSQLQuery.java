/* Generated By:JJTree: Do not edit this line. ASTFullSQLQuery.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.biopcd.parser;

public
class ASTFullSQLQuery extends SimpleNode {
  public ASTFullSQLQuery(int id) {
    super(id);
  }

  public ASTFullSQLQuery(PCD p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PCDVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=e7c9fe48fe3f59b34898b0c163557081 (do not edit this line) */
