/* Generated By:JJTree: Do not edit this line. ASTtextFields.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.biopcd.parser;

public
class ASTtextFields extends SimpleNode {
  public ASTtextFields(int id) {
    super(id);
  }

  public ASTtextFields(PCD p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PCDVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=f952d9b4ff9019efb1e6dd36492a2016 (do not edit this line) */
