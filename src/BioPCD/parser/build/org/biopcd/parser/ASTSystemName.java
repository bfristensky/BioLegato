/* Generated By:JJTree: Do not edit this line. ASTSystemName.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.biopcd.parser;

public
class ASTSystemName extends SimpleNode {
  public ASTSystemName(int id) {
    super(id);
  }

  public ASTSystemName(PCD p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PCDVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=c2e4c409533b4e9760164d52aef0c1f5 (do not edit this line) */
