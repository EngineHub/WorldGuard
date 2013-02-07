// $Id$
/*
 * This file is a part of WorldGuard.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldGuard team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY), without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldguard.region;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.sk89q.worldguard.Classifier;


/**
 * Provides support for allowing an attribute to be applied differently
 * depending on a given {@link Classifier}.
 * <p>
 * For example, a 'vampirism' flag may be true when a given user is part of
 * a 'Vampires' group but it would be false for other users. That, however,
 * is a simple example with only two different states for two different groups,
 * but this class also allows for a large number of different states applied
 * to many different groups. If there are conflicts, the first 'entry' takes
 * effect.
 * <p>
 * Use of this class is illustrated as:
 * {@code new Stereotype<AnotherAttribute>(new AnotherAttribute("yourplugin.yourattr"))}.
 * It is noteworthy to mention that the name of the Stereotype (which is
 * a regular attribute too) is copied, in this case, from the name of
 * AnotherAttribute.
 */
public class Stereotype<T extends Attribute> extends Attribute {
    
    private static final byte VERSION = 0;
    
    private Attribute child;
    private List<Stereotype<T>> entries = new ArrayList<Stereotype<T>>();

    /**
     * No-arg constructor.
     */
    public Stereotype() {
    }

    /**
     * Construct an instance using the given child attribute.
     * <p>
     * The name of this attribute is copied from the child attribute's name.
     * 
     * @param child child attribute
     */
    public Stereotype(Attribute child) {
        Validate.notNull(child);
        
        this.child = child;
        setName(child.getName());
    }

    @Override
    public void read(DataInputStream in, int len) throws IOException {
        
        // @TODO: Read the stereotype
        
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeByte(VERSION);
        out.writeUTF(child.getClass().getCanonicalName());
        
        // @TODO: Write the stereotype
    }
    
    /**
     * Stores the necessary data for {@link Stereotype} entries.
     */
    public class Entry {
        
        private T attribute;
        private Classifier classifier;
        
        /**
         * Create a new instance with the given attribute and classifier.
         * 
         * @param attribute attribute
         * @param classifier classifier
         */
        public Entry(T attribute, Classifier classifier) {
            setAttribute(attribute);
            setClassifier(classifier);
        }
        
        /**
         * Get the attribute for this entry.
         * 
         * @return the attribute
         */
        public T getAttribute() {
            return attribute;
        }
        
        /**
         * Set the attribute for this entry.
         * 
         * @param attribute the attribute
         */
        public void setAttribute(T attribute) {
            Validate.notNull(attribute);
            
            this.attribute = attribute;
        }

        /**
         * Get the classifier for this entry.
         * 
         * @return classifier
         */
        public Classifier getClassifier() {
            return classifier;
        }

        /**
         * Set the classifier for this entry.
         * 
         * @param classifier the classifier
         */
        public void setClassifier(Classifier classifier) {
            Validate.notNull(classifier);
            
            this.classifier = classifier;
        }
    }

}
