package org.sdpa.protocol;


public enum SDPAEventOp{
	
	OPRATOR_NON		( 0 ),
	OPRATOR_ISEQUAL (1),	/* = */
	OPRATOR_ADD		(2),		/* + */
	OPRATOR_SUB		(3), 		/* - */
	OPRATOR_BITAND	(4),				/* & */
	OPRATOR_BITOR	(5), 		/* | */
	OPRATOR_BITWISE	(6),	/* ^ */
	OPRATOR_GREATER	(7),    /* > */
	OPRATOR_LESS	(8),		/* < */
	OPRATOR_EQUALGREATER	(9),	/* >= */
	OPRATOR_EQUALLESS	(10);	/* <= 8 */
	
    protected int value;

    private SDPAEventOp(int value) {
        this.value = value;
    }

    /**
     * @return the
     * value
     */
    public int getValue() {
        return value;
    }
    

    
}