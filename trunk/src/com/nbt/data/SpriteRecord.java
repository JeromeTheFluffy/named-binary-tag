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

package com.nbt.data;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

import com.tag.Cache;

import resources.Resource;

public class SpriteRecord extends SimpleRecord implements Sprite {

    private static Grid256 blockGrid;
    private static Grid256 itemGrid;

    private Cache<Float, BufferedImage> cache = new Cache<Float, BufferedImage>() {

	@Override
	public BufferedImage apply(Float key) {
	    BufferedImage image = getImage();
	    BufferedImage rgb = toRGB(image);
	    RescaleOp op = new RescaleOp(key, 0, null);
	    op.filter(rgb, rgb);
	    return rgb;
	}

	private BufferedImage toRGB(BufferedImage image) {
	    int width = image.getWidth(), height = image.getHeight();
	    BufferedImage image2 = new BufferedImage(width, height,
		    BufferedImage.TYPE_INT_RGB);
	    Graphics2D g = image2.createGraphics();
	    int x = 0, y = 0;
	    g.drawImage(image, x, y, null);
	    g.dispose();
	    return image2;
	}

    };

    public SpriteRecord(String[] row) {
	super(row);
    }

    public boolean isBlock() {
	int id = getID();
	return (id >= 0 && id <= 0xFF);
    }

    @Override
    public BufferedImage getImage() {
	Grid256 grid = isBlock() ? getBlockGrid() : getItemGrid();
	int index = getIconIndex();
	return grid.subimage(index);
    }

    public BufferedImage getImage(float darkness) {
	return cache.get(darkness);
    }

    private static Grid256 getBlockGrid() {
	if (blockGrid == null)
	    blockGrid = createGrid("images/terrain.png");
	return blockGrid;
    }

    private static Grid256 getItemGrid() {
	if (itemGrid == null)
	    itemGrid = createGrid("images/items.png");
	return itemGrid;
    }

    private static Grid256 createGrid(String name) {
	Resource resource = new Resource();
	BufferedImage image = resource.getImage(name);
	return new Grid256(image);
    }

}