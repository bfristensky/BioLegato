/* Generated By:JJTree: Do not edit this line. ASTdecimalFields.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.biopcd.parser;

public
class ASTdecimalFields extends SimpleNode {
  public ASTdecimalFields(int id) {
    super(id);
  }

  public ASTdecimalFields(PCD p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PCDVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=297a9f626aa0aefc9f17205f3d93d195 (do not edit this line) */
