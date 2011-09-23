/*
 * Copyright 2011 Taggart Spilman. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY Taggart Spilman ''AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL Taggart Spilman OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of Taggart Spilman.
 */

package com.nbt;

import java.util.Arrays;

public class ByteWrapper implements Node {

	private final byte[] bytes;
	private final int index;

	public ByteWrapper(byte[] bytes, int index) {
		this.bytes = bytes;
		this.index = index;
	}

	@Override
	public boolean isCellEditable(int column) {
		switch (column) {
			case Node.COLUMN_VALUE:
				return true;
		}
		return false;
	}

	@Override
	public Object getValueAt(int column) {
		switch (column) {
			case Node.COLUMN_KEY:
				return index;
			case Node.COLUMN_VALUE:
				return bytes[index];
		}
		return null;
	}

	@Override
	public void setValueAt(Object value, int column) {
		if (value instanceof Byte) {
			switch (column) {
				case Node.COLUMN_VALUE:
					bytes[index] = (Byte) value;
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ByteWrapper other = (ByteWrapper) obj;
		if (!Arrays.equals(bytes, other.bytes))
			return false;
		if (index != other.index)
			return false;
		return true;
	}

}