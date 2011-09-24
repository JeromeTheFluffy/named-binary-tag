package org.jnbt;

import com.nbt.NBTBranch;
import com.nbt.ByteWrapper;
import com.tag.HexUtils;

/*
 * JNBT License
 * 
 * Copyright (c) 2010 Graham Edgecombe All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * * Neither the name of the JNBT team nor the names of its contributors may be
 * used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * The <code>TAG_Byte_Array</code> tag.
 * 
 * @author Graham Edgecombe
 * @author Taggart Spilman
 * 
 */
public class ByteArrayTag extends Tag<byte[]> implements NBTBranch {

    public static final String TAG_NAME = "TAG_Byte_Array";

    public ByteArrayTag(String name) {
	super(name);
    }

    public ByteArrayTag(String name, byte[] value) {
	super(name, value);
    }

    @Override
    protected byte[] createDefaultValue() {
	return new byte[] {};
    }

    @Override
    public boolean isCellEditable(int column) {
	return false;
    }

    @Override
    public Object getValueAt(int column) {
	switch (column) {
	case COLUMN_VALUE:
	    byte[] bytes = getValue();
	    return bytes.length + " bytes";
	default:
	    return super.getValueAt(column);
	}
    }

    @Override
    public Object getChild(int index) {
	byte[] value = getValue();
	return new ByteWrapper(value, index);
    }

    @Override
    public int getChildCount() {
	byte[] value = getValue();
	return value.length;
    }

    @Override
    public int getIndexOfChild(Object child) {
	if (child instanceof Integer) {
	    return (Integer) child;
	}
	return -1;
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder(TAG_NAME);
	String name = getName();
	if (!name.isEmpty())
	    sb.append("(\"").append(name).append("\")");
	sb.append(":");
	for (byte b : getValue()) {
	    sb.append(" ");
	    String hex = HexUtils.byteToHex(b);
	    sb.append(hex);
	}
	return sb.toString();
    }

}