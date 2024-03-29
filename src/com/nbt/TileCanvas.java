/**
 * Copyright 2011 Taggart Spilman
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.nbt;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.Validate;
import org.jnbt.Tag;

import resources.Resource;

import com.nbt.data.Register;
import com.nbt.data.SpriteRecord;
import com.tag.MouseDragAndDrop;
import com.terrain.Block;
import com.terrain.Chunk;
import com.terrain.Player;
import com.terrain.Region;
import com.terrain.World;

@SuppressWarnings("serial")
public class TileCanvas extends JComponent {

    public static final String KEY_TILE_X = "tileX", KEY_TILE_Z = "tileZ",
	    KEY_ALTITUDE = "altitude", KEY_WIDTH = "width",
	    KEY_HEIGHT = "height";

    private static final Register<SpriteRecord> register;
    static {
	register = new Register<SpriteRecord>() {
	    @Override
	    protected SpriteRecord createRecord(String[] row) {
		return new SpriteRecord(row);
	    }
	};
	Resource resource = new Resource();
	List<String[]> blocks = resource.getCSV("csv/blocks.csv");
	register.load(blocks);
	List<String[]> items = resource.getCSV("csv/items.csv");
	register.load(items);
    }

    private static final int MIN_TILE_WIDTH = 1, MIN_TILE_HEIGHT = 1;
    // TODO: this should be calculated
    private static final int SPRITE_SIZE = 16;

    private final World world;
    private int x, z, altitude;
    private int tileWidth = 16, tileHeight = 16;

    private HUD hud;

    public TileCanvas(Region region) {
	this(new TileWorld(region));
    }

    public TileCanvas(final World world) {
	Validate.notNull(world, "world must not be null");
	this.world = world;

	setFocusable(true);
	addComponentListener(new ComponentAdapter() {
	    @Override
	    public void componentResized(ComponentEvent e) {
		int tileWidth = calculateWidth();
		setTileWidth(tileWidth);

		int tileHeight = calculateHeight();
		setTileHeight(tileHeight);

		save();
	    }
	});
	addKeyListener(new KeyAdapter() {
	    @Override
	    public void keyPressed(KeyEvent e) {
		TileCanvas.this.keyPressed(e);
	    }
	});
	addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		Point point = e.getPoint();
		int x = pixelsToTile(point.x);
		int z = pixelsToTile(point.y);
		int tileX = x + getTileX();
		int tileZ = z + getTileZ();
		int y = getAltitude();
		Block block = world.getBlock(tileX, y, tileZ);
		blockClicked(block);
	    }
	});
	addMouseWheelListener(new MouseWheelListener() {
	    @Override
	    public void mouseWheelMoved(MouseWheelEvent e) {
		int amount = e.getWheelRotation();
		int altitude = getAltitude();
		setAltitude(amount + altitude);

		updateXYZ();
		doRepaint();
		save();
	    }
	});
	addMouseMotionListener(new MouseMotionAdapter() {
	    @Override
	    public void mouseMoved(MouseEvent e) {
		updateXYZ();
	    }
	});
	new MouseDragAndDrop(this) {

	    private int tileX, tileZ;

	    @Override
	    public void selected(MouseEvent e) {
		this.tileX = getTileX();
		this.tileZ = getTileZ();
	    }

	    @Override
	    public void dragged(MouseEvent e) {
		MouseEvent startEvent = getStartEvent();
		Point startPt = startEvent.getPoint();
		Point releasePt = e.getPoint();
		int x = tileX
			+ (pixelsToTile(startPt.x) - pixelsToTile(releasePt.x));
		int z = tileZ
			+ (pixelsToTile(startPt.y) - pixelsToTile(releasePt.y));
		setTileX(x);
		setTileZ(z);

		updateXYZ();
		doRepaint();
	    }

	    @Override
	    public void dropped(MouseEvent press, MouseEvent release) {
		// Point startPt = press.getPoint();
		// Point releasePt = release.getPoint();
		// int x = getTileX() + pixelsToTile(startPt.x - releasePt.x);
		// int z = getTileZ() + pixelsToTile(startPt.y - releasePt.y);
		// setTileX(x);
		// setTileZ(z);
		//
		// updateXYZ();
		// doRepaint();

		save();
	    }

	}.install();

	setLayout(null);
	hud = new HUD();
	int width = 200, height = 200;
	hud.setSize(width, height);
	add(hud);
    }

    private void updateXYZ() {
	Point pt = getMousePosition();
	if (pt == null)
	    pt = new Point();

	int x = getTileX() + pixelsToTile(pt.x);
	int z = getTileZ() + pixelsToTile(pt.y);
	hud.xl.setText("X: " + x);
	hud.zl.setText("Z: " + z);
	hud.al.setText("Y: " + getAltitude());
    }

    public void save() {
	Preferences prefs = getPreferences();
	prefs.putInt(KEY_TILE_X, getTileX());
	prefs.putInt(KEY_TILE_Z, getTileZ());
	prefs.putInt(KEY_ALTITUDE, getAltitude());
	prefs.putInt(KEY_WIDTH, getTileWidth());
	prefs.putInt(KEY_HEIGHT, getTileHeight());
    }

    public void restore() {
	Preferences prefs = getPreferences();

	int def = 0;
	int x = prefs.getInt(KEY_TILE_X, def);
	setTileX(x);

	int z = prefs.getInt(KEY_TILE_Z, def);
	setTileZ(z);

	def = 70;
	int altitude = prefs.getInt(KEY_ALTITUDE, def);
	setAltitude(altitude);

	def = 16;
	int width = prefs.getInt(KEY_WIDTH, def);
	setTileWidth(width);

	int height = prefs.getInt(KEY_HEIGHT, def);
	setTileHeight(height);
    }

    @Override
    protected void paintComponent(Graphics g) {
	super.paintComponent(g);

	Graphics2D g2d = (Graphics2D) g.create();

	g2d.setColor(Color.BLACK);
	int x = 0, y = 0;
	Dimension size = getSize();
	g2d.fillRect(x, y, size.width, size.height);

	final int altitude = getAltitude();
	for (int w = 0; w < getTileWidth(); w++) {
	    x = w * SPRITE_SIZE;
	    for (int h = 0; h < getTileHeight(); h++) {
		y = h * SPRITE_SIZE;

		int xOffset = w + getTileX();
		int zOffset = h + getTileZ();
		BufferedImage tile = getBackgroundTile(xOffset, altitude,
			zOffset);
		if (tile != null)
		    g2d.drawImage(tile, x, y, null);
	    }
	}

	g2d.dispose();
    }

    protected void keyPressed(KeyEvent e) {
	final int keyCode = e.getKeyCode();
	switch (keyCode) {
	case KeyEvent.VK_UP:
	    int z = getTileZ();
	    setTileZ(z - 1);
	    break;
	case KeyEvent.VK_DOWN:
	    z = getTileZ();
	    setTileZ(z + 1);
	    break;
	case KeyEvent.VK_LEFT:
	    int x = getTileX();
	    setTileX(x - 1);
	    break;
	case KeyEvent.VK_RIGHT:
	    x = getTileX();
	    setTileX(x + 1);
	    break;
	case KeyEvent.VK_PAGE_UP:
	    int altitude = getAltitude();
	    setAltitude(altitude + 1);
	    break;
	case KeyEvent.VK_PAGE_DOWN:
	    altitude = getAltitude();
	    setAltitude(altitude - 1);
	    break;
	}

	updateXYZ();
	doRepaint();
	save();
    }

    public void doRepaint() {
	Runnable runnable = new Runnable() {
	    @Override
	    public void run() {
		repaint();
	    }
	};
	if (SwingUtilities.isEventDispatchThread())
	    runnable.run();
	else
	    SwingUtilities.invokeLater(runnable);
    }

    protected void blockClicked(Block block) {

    }

    public int pixelsToTile(int pixels) {
	return pixels / SPRITE_SIZE;
    }

    public SpriteRecord getTile(int x, int y, int z) {
	Block block = world.getBlock(x, y, z);
	if (block == null)
	    return null;

	int id = block.getBlockID();
	return register.getRecord(id);
    }

    public BufferedImage getBackgroundTile(int x, int y, int z) {
	float darkness = 1;
	for (; y >= Block.MIN_Y; y--) {
	    SpriteRecord sprite = getTile(x, y, z);
	    if (sprite != null)
		return darkness < 1 ? sprite.getImage(darkness) : sprite
			.getImage();
	    darkness -= .05f;
	}
	return null;
    }

    public int getTileX() {
	return x;
    }

    public void setTileX(int x) {
	this.x = x;
    }

    public int getTileZ() {
	return z;
    }

    public void setTileZ(int z) {
	this.z = z;
    }

    public int getAltitude() {
	return altitude;
    }

    public void setAltitude(int altitude) {
	this.altitude = validate(altitude, Block.MIN_Y, Block.MAX_Y - 1);
    }

    public int getTileWidth() {
	int tileWidth = calculateWidth();
	return Math.max(this.tileWidth, tileWidth);
    }

    private int calculateWidth() {
	int width = getWidth();
	return (width / SPRITE_SIZE) + 1;
    }

    public void setTileWidth(int tileWidth) {
	this.tileWidth = validate(tileWidth, MIN_TILE_WIDTH, Integer.MAX_VALUE);
	updateSize();
    }

    public int getTileHeight() {
	int tileHeight = calculateHeight();
	return Math.max(this.tileHeight, tileHeight);
    }

    private int calculateHeight() {
	int height = getHeight();
	return (height / SPRITE_SIZE) + 1;
    }

    public void setTileHeight(int tileHeight) {
	this.tileHeight = validate(tileHeight, MIN_TILE_HEIGHT,
		Integer.MAX_VALUE);
	updateSize();
    }

    private void updateSize() {
	int width = getTileWidth() * SPRITE_SIZE;
	int height = getTileHeight() * SPRITE_SIZE;
	super.setPreferredSize(new Dimension(width, height));
    }

    public Preferences getPreferences() {
	String name = world.getName();
	return Preferences.userNodeForPackage(getClass()).node(name);
    }

    private static int validate(int value, int min, int max) {
	value = Math.min(value, max);
	value = Math.max(value, min);
	return value;
    }

    private static class HUD extends JPanel {

	public final JLabel xl;
	public final JLabel zl;
	public final JLabel al;

	public HUD() {
	    super();

	    BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
	    setLayout(boxLayout);

	    setOpaque(false);

	    add(xl = createLabel(" "));
	    add(zl = createLabel(" "));
	    add(al = createLabel(" "));
	}

	protected JLabel createLabel(String text) {
	    JLabel label = new JLabel(text);
	    label.setHorizontalAlignment(JLabel.LEADING);
	    label.setForeground(Color.WHITE);
	    label.setOpaque(false);
	    return label;
	}

    }

    public static class TileWorld implements World {

	private final Region region;

	public TileWorld(Region region) {
	    Validate.notNull(region, "region must not be null");
	    this.region = region;
	}

	@Override
	public Tag<?> getLevel() {
	    throw new IllegalStateException("stub");
	}

	@Override
	public Player getPlayer(String name) {
	    throw new IllegalStateException("stub");
	}

	@Override
	public List<Player> getPlayers() {
	    throw new IllegalStateException("stub");
	}

	@Override
	public Region getRegion(int regionX, int regionZ) {
	    throw new IllegalStateException("stub");
	}

	@Override
	public List<Region> getRegions() {
	    throw new IllegalStateException("stub");
	}

	@Override
	public Region getRegionFor(int chunkX, int chunkZ) {
	    throw new IllegalStateException("stub");
	}

	@Override
	public Chunk getChunkFor(int x, int z) {
	    int chunkX = divide(x, Block.MAX_X);
	    int chunkZ = divide(z, Block.MAX_Z);
	    try {
		return region.getChunk(chunkX, chunkZ);
	    } catch (Exception e) {
		//e.printStackTrace();
	    }
	    return null;
	}

	private int divide(int dividend, int divisor) {
	    int quotient = dividend / divisor;
	    if (dividend < 0 && divisor >= 0)
		quotient--;
	    return quotient;
	}

	@Override
	public Block getBlock(int x, int y, int z) {
	    Chunk chunk = getChunkFor(x, z);
	    if (chunk == null) {
		// System.err.println("chunk is null!");
		return null;
	    }

	    int localX = modulus(x, Block.MAX_X);
	    int localZ = modulus(z, Block.MAX_Z);
	    return chunk.getBlock(localX, y, localZ);
	}

	private int modulus(int dividend, int divisor) {
	    int modulo = dividend % divisor;
	    if (dividend < 0)
		modulo--;
	    return modulo;
	}

	@Override
	public String getName() {
	    return region.getName();
	}

    }

}