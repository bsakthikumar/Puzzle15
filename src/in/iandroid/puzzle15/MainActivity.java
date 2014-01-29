package in.iandroid.puzzle15;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.util.TypedValue;

public class MainActivity extends Activity {

	private int dx;
	private int dy;
	private int cx;
	private int cy;
	private int dir;
	private int down;
	private int block_to_move;
	
	private Point blockSize;
	private Point screenSize;
	private Point startMargin;
	private Point iconSize;
	private Point iconStartMargin;
	
	/* Listener for movement of blocks */
	private OnTouchListener touchListener;
	
	/* Listener for menu buttons */
	private OnClickListener newListener;
	private OnClickListener aboutListener;
	private OnClickListener refreshListener;
	private OnClickListener exitListener;
	
	/* Number of Times a block has to be moved */
	private final int SHUFFLE_TIMES = 1000;
	
	/* Movable directions */
	private final int NONE = 0;
	private final int LEFT = 1;
	private final int RIGHT = 2;
	private final int UP = 3;
	private final int DOWN = 4;
	
	/* Space between two blocks */
	private final int GAP = 4;
	
	/* Blocks per row, except last */
	private final int R_BLOCKS = 4;
	
	/* IDs of each block, defined in res/values/ids.xml */
	private final int[][] ids = {{R.id.block00, R.id.block01, R.id.block02, R.id.block03},
								 {R.id.block10, R.id.block11, R.id.block12, R.id.block13},
								 {R.id.block20, R.id.block21, R.id.block22, R.id.block23},
								 {R.id.block30, R.id.block31, R.id.block32, R.id.block33}};
	
	/* Current layout in form of numbers in array; initial values given */
	private int[][] currState = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 0}};
	
	/* Previous game */
	private int[][] prevState = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 0}};
	
	private final Context c = this;
	
	private int moves = 0;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        down = 0;
        dx = dy = 0;
        cx = cy = 0;

        setContentView(R.layout.activity_main);
        
        /* Get the block size to be drawn */
        getBlockSize();
        
        /* Create the touch listener */
        setListener();
        
        /* Create Menu bar */
        createMenu();
        
        /* Create the Counter for number of moves */
        createCounter();
        
        /* Place the blocks */
        for (int i = 0; i < R_BLOCKS; i++)
        	for (int j = 0; j < R_BLOCKS; j++)
        		addBlock(i, j);
        
        /* Shuffle before starting the game */
        shuffle();
		resetLayout();
		storeCurrentState();
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	resetLayout();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	System.exit(0);
    }

    
    private void getBlockSize() {
        /* Get the screen size */
        Display display = getWindowManager().getDefaultDisplay();
		screenSize = new Point();
		display.getSize(screenSize);
		
		/* Calculate the icon size */
		iconSize = new Point();
		
		/* Get the space available for layout; weight 6 out of 8 */
		int availy = (screenSize.y / 8) * 6;

		/* If available space is less than width */
		if (availy < screenSize.x) {
			/* Calculate the block size based on available space which is smaller */
			blockSize = new Point();
			blockSize.x = (availy - GAP * (R_BLOCKS + 1)) / R_BLOCKS;	// 4 blocks, 5 gaps
			blockSize.y = blockSize.x;
			
			/* Calculate the start x and y margin */
			startMargin = new Point();
			startMargin.x = (screenSize.x - availy) / 2 + GAP;
			startMargin.y = (screenSize.y / 8);
			
			return;
		}
		
		/* Calculate the block size based on Screen size */
		blockSize = new Point();
		blockSize.x = (screenSize.x - GAP * (R_BLOCKS + 1)) / R_BLOCKS;	// 4 blocks, 5 gaps
		blockSize.y = blockSize.x;
		
		/* Calculate the start x and y margin */
		startMargin = new Point();
		startMargin.x = GAP;
		startMargin.y = (screenSize.y / 8) + (availy - screenSize.x) / 2;
		
		/* For icon placement */
		iconSize = new Point();
		iconSize.y = (screenSize.y / 8);
		iconSize.x = (screenSize.x - GAP) / 3 - GAP;
		
		iconStartMargin = new Point();
		iconStartMargin.x = GAP;
		iconStartMargin.y = (screenSize.y / 8) * 7 + GAP;
    }
    
    /* Adds a block to the layout, called only during creation, runtime calls to resetLayout */
    private void addBlock(int i, int j) {
    	
    	/* Skip the last block to be drawn */
    	if (i == R_BLOCKS - 1 && j == R_BLOCKS - 1) {
    		return;
    	}
    	
    	/* Get the current layout */
    	RelativeLayout l = (RelativeLayout)findViewById(R.id.layoutBoard);
    	
    	/* Initialize the layout parameters with block size */
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(blockSize.x, blockSize.y);
        
        /* Set the top and left margin appropriately */
        params.topMargin  = startMargin.y + j * blockSize.y + j * GAP;
        params.leftMargin = startMargin.x + i * blockSize.x + i * GAP;
        
        /* Use specific font */
        Typeface fontFace = Typeface.createFromAsset(getAssets(), "fonts/DROIDSANS.TTF");
        
        /* Create the Button */
        Button b = new Button(this);
        
        /* Configure the button */
        b.setId(ids[i][j]);
        b.setTypeface(fontFace);
        b.setLayoutParams(params);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setBackgroundResource(R.drawable.block);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, blockSize.x / 3);
        b.setText(Integer.toString(j * R_BLOCKS + i + 1));
        b.setOnTouchListener(touchListener);
        
        /* Add to layout */
        l.addView(b);
    }
    
    /* Resets the blocks in appropriate position based on back-end after shuffle */
    private void resetLayout() {
    	Button b;
    	RelativeLayout.LayoutParams params;
    	
    	for (int i = 0; i < R_BLOCKS; i++) {
    		for (int j = 0; j < R_BLOCKS; j++) {
    			/* Get the current block */
    			int curr_block = currState[j][i];

    			if (curr_block == 0)
    				continue;
    			
    			int x = (curr_block - 1) % 4;
    			int y = (curr_block - 1) / 4;
    			
    			b = (Button)findViewById(ids[x][y]);
    			params = (RelativeLayout.LayoutParams)b.getLayoutParams();
    			
    			/* Set the top and left margin appropriately */
    	        params.topMargin  = startMargin.y + j * blockSize.y + j * GAP;
    	        params.leftMargin = startMargin.x + i * blockSize.x + i * GAP;
    	        
    	        b.setLayoutParams(params);
    		}
    	}
    	return;
    }
    
    @Override
    public void onBackPressed() {
    	exitPrompt();    	
    }
    
    private void setListener() {
    	touchListener = new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					Button b = (Button)v;
					
					/* No support for multi-touch */
					if (down == 0) {
						down = Integer.parseInt(b.getText().toString());
					} else {
						break;
					}
					
					/* Store the margin; assuming at ACTION_UP will be set to proper values */
					dx = params.leftMargin;
					dy = params.topMargin;
					
					/* Getting current x and y; useful in ACTION_MOVE */
					cx = (int) event.getRawX();
					cy = (int) event.getRawY();
					
					/* Block number; obtain from label of button */
					block_to_move = down;
					
					/* Get direction to move; used in ACTION_MOVE, ACTION_UP */
					dir = getMoveDir(block_to_move);
					
				}/* ACTION_DOWN */
				break;
				
				case MotionEvent.ACTION_UP: {
					Button b = (Button)v;
					
					int up = Integer.parseInt(b.getText().toString());
					
					if (up == down)
						down = 0;
					else
						break;
					
					if (dir == NONE)
						break;
					
					if (dir == DOWN) {
						/* Moved greater than half, update current state array, same follows for below cases */
						if (params.topMargin > (dy + blockSize.y /2)) {
							move(block_to_move, dir);
							params.topMargin = dy + blockSize.y + GAP;
							moves++;
							updateMoves();
						} else {
							/* Reset back original so that move either completes or not; no hang in middle */
							params.topMargin = dy;
						}
					} else if (dir == UP) {
						if (params.topMargin < (dy - blockSize.y /2)) {
							move(block_to_move, dir);
							params.topMargin = dy - blockSize.y - GAP;
							moves++;
							updateMoves();
						} else {
							params.topMargin = dy;
						}
					} else if (dir == RIGHT) {
						if (params.leftMargin > (dx + blockSize.x /2)) {
							move(block_to_move, dir);
							params.leftMargin = dx + blockSize.x + GAP;
							moves++;
							updateMoves();
						} else {
							params.leftMargin = dx;
						}
					} else if (dir == LEFT) {
						if (params.leftMargin < (dx - blockSize.x /2)) {
							move(block_to_move, dir);
							params.leftMargin = dx - blockSize.x - GAP;
							moves++;
							updateMoves();
						} else {
							params.leftMargin = dx;
						}
					}

					v.setLayoutParams(params);
					
					/* Check if game has ended */
					if (gameOver() == 1) {
						enableLayout(false);
						String s = "Moves: " + Integer.toString(moves) + "\n" + getResources().getString(R.string.game_over_msg);
						AlertDialog alert = new AlertDialog.Builder(c)
												.setTitle("Game Finished")
												.setMessage(s)
												.setPositiveButton("OK", null)
												.setCancelable(true)
												.setIcon(android.R.drawable.ic_dialog_info)
												.create();
					
						alert.show();
					}
						
				}/* ACTION_UP */
				break;
				
				case MotionEvent.ACTION_MOVE: {
					int x = (int)event.getRawX();
	            	int y = (int)event.getRawY();
	            	
	            	if (dir == NONE)
	            		break;
	            	
	            	Button b = (Button)v;
					
					int move = Integer.parseInt(b.getText().toString());
					
					if (move != down)
						break;
	            	
	            	if (dir == DOWN) {
	            		/* Increment y; Look for bounds */
	            		params.topMargin += y - cy;
	            		if (params.topMargin < dy)
	            			params.topMargin = dy;
	            		if (params.topMargin > dy + blockSize.y + GAP)
	            			params.topMargin = dy + blockSize.y + GAP;
	            	} else if (dir == UP) {
	            		/* Decrement y; Look for bounds */
	            		params.topMargin += y - cy;
	            		if (params.topMargin > dy)
	            			params.topMargin = dy;
	            		if (params.topMargin < dy - blockSize.y - GAP)
	            			params.topMargin = dy - blockSize.y - GAP;
	            	} else if (dir == RIGHT) {
	            		/* Increment x; Look for bounds */
	            		params.leftMargin += x - cx;
	            		if (params.leftMargin < dx)
	            			params.leftMargin = dx;
	            		if (params.leftMargin > dx + blockSize.x + GAP)
	            			params.leftMargin = dx + blockSize.x + GAP;
	            	} else if (dir == LEFT) {
	            		/* Decrement x; Look for bounds */
	            		params.leftMargin += x - cx;
	            		if (params.leftMargin > dx)
	            			params.leftMargin = dx;
	            		if (params.leftMargin < dx - blockSize.x - GAP)
	            			params.leftMargin = dx - blockSize.x - GAP;
	            	}
	                
	            	/* Reset to current; after move */
	                cx = x;
	                cy = y;
	                
	                v.setLayoutParams(params);
				}/* ACTION_MOVE */
				break;
				
				}/* switch */
				return false;
			}
		};
		
		newListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				TextView b = (TextView)v;
				b.setEnabled(false);
				if (gameOver() == 1) {
					enableLayout(true);
					shuffle();
					resetLayout();
					storeCurrentState();
					resetMoves();
					updateMoves();
				} else {
					AlertDialog.Builder alert = new AlertDialog.Builder(c);
					alert.setTitle("Warning");
					alert.setMessage(R.string.new_game_msg);
					alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							restoreDefaults();
							enableLayout(true);
							shuffle();
							resetLayout();
							storeCurrentState();
							resetMoves();
							updateMoves();
						}
					});
					alert.setNegativeButton("No", null);
					alert.setCancelable(false);
					alert.setIcon(android.R.drawable.ic_dialog_alert);
					alert.create();
					alert.show();
				}
				b.setEnabled(true);
			}
		};
		
		refreshListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView b = (TextView)v;
				b.setEnabled(false);
				if (gameOver() == 1) {
					enableLayout(true);
					restoreCurrentState();
					resetLayout();
					resetMoves();
					updateMoves();
				} else if (moves == 0) {
					AlertDialog.Builder alert = new AlertDialog.Builder(c);
					alert.setTitle("Info");
					alert.setMessage(R.string.no_moves_msg);
					alert.setPositiveButton("OK", null);
					alert.setCancelable(true);
					alert.setIcon(android.R.drawable.ic_dialog_info);
					alert.create();
					alert.show();
				} else {
					AlertDialog.Builder alert = new AlertDialog.Builder(c);
					alert.setTitle("Warning");
					alert.setMessage(R.string.restart_game_msg);
					alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							enableLayout(true);
							restoreCurrentState();
							resetLayout();
							resetMoves();
							updateMoves();
						}
					});
					alert.setNegativeButton("No", null);
					alert.setCancelable(false);
					alert.setIcon(android.R.drawable.ic_dialog_alert);
					alert.create();
					alert.show();
				}
				b.setEnabled(true);
			}
		};
		
		aboutListener = new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				TextView b = (TextView)v;
				b.setEnabled(false);
				SpannableString s = new SpannableString(c.getText(R.string.about_msg));
				Linkify.addLinks(s, Linkify.WEB_URLS);
				
				AlertDialog alert = new AlertDialog.Builder(c)
											.setTitle("About")
											.setMessage(s)
											.setPositiveButton("OK", null)
											.setCancelable(true)
											.setIcon(android.R.drawable.ic_dialog_info)
											.create();
										
				alert.show();
				((TextView)alert.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
				b.setEnabled(true);
			}
		};
		
		exitListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				exitPrompt();
			}
		};
    }
    
    /* Returns the movable direction for a block at (i, j), returns NONE if cannot move */
    private int canMove(int i, int j) {
    	int x;
    	
    	/* Look UP and DOWN (i - 1, j) and (i + 1, j) */
    	for (x = -1; x <= 1; x += 2) {
    		int a = x + i;

    		/* Out of bounds */
    		if (a < 0 || a > R_BLOCKS - 1)
    			continue;
    	
    		/* If free block present, return direction */
    		if (currState[a][j] == 0) {
    			if (a < i)
    				return UP;
    			else
    				return DOWN;
    		}
    	}

    	/* Look LEFT and RIGHT (i, j - 1) and (i, j + 1) */
    	for (x = -1; x <= 1; x += 2) {
    		int a = x + j;

    		/* Out of bounds */
    		if (a < 0 || a > R_BLOCKS - 1)
    			continue;
    	
    		/* If free block present, return direction */
    		if (currState[i][a] == 0) {
    			if (a < j)
    				return LEFT;
    			else
    				return RIGHT;
    		}
    	}
    	
    	return NONE;
    }
    
    /* Returns the movable direction of given block number */
    private int getMoveDir(int block) {
    	/* Find the i and j of current block */
    	for (int i = 0; i < R_BLOCKS; i++)
    		for (int j = 0; j < R_BLOCKS; j++)
    			if (currState[i][j] == block)
    				return canMove(i, j);
    	
    	/* Cannot go out of bounds, just for compilation error */
    	return NONE;
    }
    
    /* Moves the block in back-end state array */
    private void move(int block, int dir) {
    	for (int i = 0; i < R_BLOCKS; i++)
    		for (int j = 0; j < R_BLOCKS; j++)
    			/* Update the current state array, after successful front end move */
    			if (currState[i][j] == block) {
    				if (dir == DOWN)
    					swap(currState, i, j, i + 1, j);
    				else if (dir == UP)
    					swap(currState, i, j, i - 1, j);
    				else if (dir == LEFT)
    					swap(currState, i, j, i, j - 1);
    				else if (dir == RIGHT)
    					swap(currState, i, j, i, j + 1);
    				return;
    			}
    }

    /* Swap two nos in 2D array; a[x][y] with a[x1][y1] */
    private void swap(int[][] a, int x, int y, int x1, int y1) {
    	int t = a[x][y];
    	a[x][y] = a[x1][y1];
    	a[x1][y1] = t;
    }
    
    /* Shuffles the blocks for a new game */
    private void shuffle() {
    	/* At most only 4 blocks can be moved */
    	int[] movableBlocks = {0, 0, 0, 0};
    	
    	/* Number of blocks currently movable; at max 4 */
    	int numMovable;
    	
    	/* Last moved block; don't move again */
    	int lastMovedBlock = 0;
    	
    	for (int i = 0; i < SHUFFLE_TIMES; i++) {
    		numMovable = 0;
    		
    		/* Load the list of movable blocks excluding last moved */
    		for (int j = 1; j < R_BLOCKS * R_BLOCKS; j++) {
    			
    			if (j == lastMovedBlock) {
    				continue;
    			}
    			
    			if (getMoveDir(j) != NONE) {
    				movableBlocks[numMovable] = j;
    				numMovable++;
    			}
    		}
    		
    		/* Get a random block to move from the array */
    		int randIndex = (int)(Math.random() * numMovable);
    		int dir = getMoveDir(movableBlocks[randIndex]);
    		
    		/* If only one block can move, set randIndex to that block */
    		if (numMovable == 1)
    			randIndex = 0;
    		
    		/* dir cannot be NONE; just safer way of doing things */
    		if (dir != NONE) {
    			move(movableBlocks[randIndex], dir);
    			lastMovedBlock = movableBlocks[randIndex];
    		}
    		
    	}
    	
    	/* If puzzle came as initial layout; re shuffle */
    	if (gameOver() == 1)
    		shuffle();
    	
    }
    
    /* Determines whether game has ended */
    private int gameOver() {
    	for (int i = 0; i < R_BLOCKS; i++)
    		for (int j = 0; j < R_BLOCKS; j++) {
    			if (i == R_BLOCKS - 1 && j == R_BLOCKS - 1)
    				continue;
    			if (currState[i][j] != (j + i * R_BLOCKS + 1))
    				return 0;
    		}
    	
    	return 1;
    }
    
    private void createMenu() {
    	String[] s = {"New", "Restart", "About", "Exit"};
    	for (int i = 0; i < 4; i++)
    		addMenuButton(i, s[i]);
    }
    
    private void addMenuButton(int i, String s) {

    	OnClickListener[] clickListeners = {newListener, refreshListener, aboutListener, exitListener};
    	
    	/* Get the current layout */
    	RelativeLayout l = (RelativeLayout)findViewById(R.id.layoutBoard);
    	
    	/* Initialize the layout parameters with block size */
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(iconSize.x, iconSize.y);
        
        /* Set the top and left margin appropriately */
        if (i < 3) {
        	params.topMargin  = iconStartMargin.x;
        	params.leftMargin = (i + 1) * GAP + i * iconSize.x;
        } else {
        	params.topMargin  = iconStartMargin.y;
        	params.leftMargin = 3 * GAP +  2 * iconSize.x;
        }

        /* Use specific font */
        Typeface fontFace = Typeface.createFromAsset(getAssets(), "fonts/DROIDSANS.TTF");
        
        /* Create the Button */
        TextView b = new TextView(this);
        
        /* Configure the button */
        b.setTypeface(fontFace);
        b.setLayoutParams(params);
        b.setTextColor(Color.WHITE);
        b.setGravity(Gravity.CENTER);
        b.setBackgroundResource(R.drawable.icon);
        b.setTextSize(iconSize.x / 10);
        b.setText(s);
        b.setClickable(true);
        b.setOnClickListener(clickListeners[i]);        
        
        /* Add to layout */
        l.addView(b);
    }
    
    /* Enable/Disable movement */
    private void enableLayout(boolean state) {
    	Button b;
    	for (int i = 0; i < R_BLOCKS; i++) {
    		for (int j = 0; j < R_BLOCKS; j++) {
    			if (i == R_BLOCKS - 1 && j == R_BLOCKS - 1)
    				continue;
    			b = (Button)findViewById(ids[i][j]);
    			b.setEnabled(state);
    		}
    	}
    }
    
    /* Stores the initial game layout when clicking new */
    private void storeCurrentState() {
    	for (int i = 0; i < R_BLOCKS; i++) {
    		for (int j = 0; j < R_BLOCKS; j++) {
    			prevState[i][j] = currState[i][j];
    		}
    	}
    }
    
    /* Loads back the saved game */
    private void restoreCurrentState() {
    	for (int i = 0; i < R_BLOCKS; i++) {
    		for (int j = 0; j < R_BLOCKS; j++) {
    			currState[i][j] = prevState[i][j];
    		}
    	}
    }

    /* Load the default layout */
    private void restoreDefaults() {
    	for (int i = 0; i < R_BLOCKS; i++) {
    		for (int j = 0; j < R_BLOCKS; j++) {
    			currState[i][j] =  j * R_BLOCKS + i + 1;
    		}
    	}
    	currState[R_BLOCKS - 1][R_BLOCKS - 1] = 0;
    }
    
    private void createCounter() {
    	/* Get the current layout */
    	RelativeLayout l = (RelativeLayout)findViewById(R.id.layoutBoard);
    	
    	/* Initialize the layout parameters with icon size */
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(iconSize.x * 2, iconSize.y);
        
        /* Set the top and left margin appropriately */
    	params.topMargin  = iconStartMargin.y;
    	params.leftMargin = GAP;

        /* Use specific font */
        Typeface fontFace = Typeface.createFromAsset(getAssets(), "fonts/DROIDSANS.TTF");
        
        /* Create the TextView */
        TextView t = new TextView(this);
        
        /* Configure the TextView */
        t.setTypeface(fontFace);
        t.setLayoutParams(params);
        t.setTextColor(Color.WHITE);
        t.setTextSize(iconSize.x / 10);
        t.setGravity(Gravity.CENTER | Gravity.LEFT);
        t.setId(R.id.lblMoves);
        t.setText("Moves : " + Integer.toString(moves));               
        
        /* Add to layout */
        l.addView(t);
    }
    
    /* Displays the new move value */
    private void updateMoves() {
    	TextView t = (TextView)findViewById(R.id.lblMoves);
    	t.setText("Moves : " + Integer.toString(moves));
    }
    
    private void resetMoves() {
    	moves = 0;
    }

    /* Asks exit prompt */
    private void exitPrompt() {
		AlertDialog.Builder alert = new AlertDialog.Builder(c);
		alert.setTitle("Warning");
		alert.setMessage(R.string.exit_game_msg);
		alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alert.setNegativeButton("No", null);
		alert.setCancelable(false);
		alert.setIcon(android.R.drawable.ic_dialog_alert);
		alert.create();
		alert.show();
    }
}
