/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2018 GuardSquare NV
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.optimize.peephole;

import proguard.classfile.ClassConstants;
import proguard.classfile.Clazz;
import proguard.classfile.ProgramClass;
import proguard.classfile.util.SimplifiedVisitor;
import proguard.classfile.visitor.ClassVisitor;
import proguard.optimize.info.WrapperClassMarker;

/**
 * This ClassVisitor inlines the wrapper classes that it visits into their
 * wrapped classes, whenever possible.
 *
 * A wrapper class can be a simple (anonymous) inner class that implements
 * some interface, for example.
 *
 * @see WrapperClassMarker
 * @see ClassMerger
 * @author Eric Lafortune
 */
public class WrapperClassMerger
extends SimplifiedVisitor
implements ClassVisitor
{
    private final boolean      allowAccessModification;
    private final ClassVisitor extraClassVisitor;


    /**
     * Creates a new WrappedClassMerger.
     *
     * @param allowAccessModification specifies whether the access modifiers
     *                                of classes can be changed in order to
     *                                merge them.
     */
    public WrapperClassMerger(boolean allowAccessModification)
    {
        this(allowAccessModification, null);
    }


    /**
     * Creates a new WrappedClassMerger.

     * @param allowAccessModification specifies whether the access modifiers
     *                                of classes can be changed in order to
     *                                merge them.
     * @param extraClassVisitor       an optional extra visitor for all
     *                                merged classes.
     */
    public WrapperClassMerger(boolean      allowAccessModification,
                              ClassVisitor extraClassVisitor)
    {
        this.allowAccessModification = allowAccessModification;
        this.extraClassVisitor       = extraClassVisitor;
    }


    // Implementations for ClassVisitor.

    public void visitProgramClass(ProgramClass programClass)
    {
        // Only merge wrapper classes that extend java.lang.Object.
        if (ClassConstants.NAME_JAVA_LANG_OBJECT.equals(programClass.getSuperName()))
        {
            // Do we have a wrapped program class?
            Clazz wrappedClass = WrapperClassMarker.getWrappedClass(programClass);
            if (wrappedClass instanceof ProgramClass)
            {
                // Try inlining the wrapper class into the wrapped class.
                new ClassMerger((ProgramClass)wrappedClass,
                                allowAccessModification,
                                false,
                                true,
                                extraClassVisitor).visitProgramClass(programClass);
            }
        }
    }
}