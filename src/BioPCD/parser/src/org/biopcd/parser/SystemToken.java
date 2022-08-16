/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.biopcd.parser;

import java.util.Set;

/**
 * Tokenizes operating system and architecture types for comparison.
 * This is used in BioPCD to evaluate whether the current program being
 * parsed is available on the current host operating system.
 **
 * @author Graham Alvare
 * @author Brian Fristensky
 */
public class SystemToken {
    /**
     * Enumeration to represent all of the system architectures
     * selectable by PCD
     */
    public static enum ARCH {
        ALL,
        X86,
        AMD64,
        SPARC;
    }
    /**
     * Enumeration to represent all of the operating systems
     * selectable by PCD
     */
    public static enum OS {
        ALL,
        LINUX,
        OSX,
        SOLARIS,
        WINDOWS,
        UNIX;
    }

    /**
     * The operating system represented by the system token.
     */
    public OS os;
    /**
     * The system architectures associated with the operating system.
     */
    public Set<ARCH> archs;

    /**
     * Creates a new operating system/architectures token.
     **
     * @param os the operating system represented by the token.
     * @param archs the architectures represented by the token.
     */
    public SystemToken (OS os, Set<ARCH> archs) {
        this.os = os;
        this.archs = archs;
    }

    /**
     * Tests for equality.
     * Two SystemToken objects are said to be equal if their OS field is
     * identical, and their archs lists contain exactly the same architectures.
     **
     * @param obj the object to compare with this SystemToken object.
     * @return true if the objects are identical in content.
     */
    public boolean equals (Object obj) {
        boolean result = false;
        SystemToken s;

        // Only test for deep equality if both objects are SystemTokens.
        if (obj instanceof SystemToken) {
            s = (SystemToken)obj;
            result = s.os == os && archs.containsAll(s.archs)
                && s.archs.containsAll(archs);
        }
        return result;
    }
}
