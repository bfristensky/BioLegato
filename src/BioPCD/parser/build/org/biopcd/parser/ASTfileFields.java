/* Generated By:JJTree: Do not edit this line. ASTfileFields.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package org.biopcd.parser;

public
class ASTfileFields extends SimpleNode {
  public ASTfileFields(int id) {
    super(id);
  }

  public ASTfileFields(PCD p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(PCDVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=5dae9d4a8d0e2add069aec489a45c49e (do not edit this line) */
