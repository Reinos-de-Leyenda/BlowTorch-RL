package es.reinosdeleyenda.flamethrower_lib.window;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import android.util.Log;
//import android.util.Log;


public class TextTree {
	
	public static final String urlFinderString = "(http://.+\\b)|(www\\..+\\b)"; 
	private final Pattern urlPattern = Pattern.compile(urlFinderString);
	private final Matcher urlMatcher = urlPattern.matcher("");
	
	public static final int MESSAGE_ADDTEXT = 0;
	Pattern colordata = Pattern.compile("\\x1B\\x5B.+m");
	Matcher colormatch = colordata.matcher("");
	
	private int modCount;
	
	public boolean debugLineAdd = false;
	//Pattern newlinelookup = Pattern.compile("\n");
	//Pattern tab = Pattern.compile(new String(new byte[]{0x09}));
	
	//public Handler addTextHandler = null;
	private boolean linkify = true;
	
	private static LinkedList<Integer> bleedColor = new LinkedList<Integer>();
	
	private int MAX_LINES = 300;
	
	private String encoding = "ISO-8859-1";
	
	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	private int breakAt = 43;
	private boolean wordWrap = true;
	
	private int brokenLineCount = 0;
	
	private int totalbytes = 0;
	private boolean cullExtraneous = true;
	
	
	public int getBrokenLineCount() {
		return brokenLineCount;
	}

	public void setBrokenLineCount(int brokenLineCount) {
		this.brokenLineCount = brokenLineCount;
	}

	public TextTree() {
		//simpleMode = pMode;
		mLines = new LinkedList<Line>();
		//LinkedList<Unit> list = new LinkedList<Unit>();
		//addTextHandler = new AddTextHandler();
		bleedColor.add(37);
		bleedColor.add(0);
	}
	
	public void addString(String str) {
		try {
			this.addBytesImplSimple(str.getBytes(encoding));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public byte[] dumpToBytes(boolean keep) {
		ByteBuffer buf = ByteBuffer.allocate(totalbytes);
		//Log.e("TREE","EXPORTING TREE:" + totalbytes + " bytes.");
		int written =0;
		//gotta do this from end to start.
		ListIterator<Line> i = mLines.listIterator(mLines.size());
		while(i.hasPrevious()) {
			Line l = i.previous();
			//Log.e("DUMP",TextTree.deColorLine(l).toString());
			Iterator<Unit> iu = l.getData().iterator();
			while(iu.hasNext()) {
				Unit u = iu.next();
				switch(u.type) {
				//if(u instanceof Text) {
				case WHITESPACE:
				case TEXT:
					buf.put(((Text)u).bin);
					written += ((Text)u).bin.length;
					break;
				//}
				//if(u instanceof Color) {
				case COLOR:
					buf.put(((Color)u).bin);
					written += ((Color)u).bin.length;
					break;
				//}
				//if(u instanceof NewLine) {
				case NEWLINE:
					buf.put(NEWLINE);
					written += 1;
					break;
				//}
				//if(u instanceof Tab) {
				case TAB:
					buf.put(TAB);
					written += 1;
					break;
				//}
				}
				
			}
		}
		
		int size = buf.position();
		//Log.e("TREE","FINISHED EXPORTING:" + written + " bytes.");
		byte[] ret = new byte[size];
		buf.rewind();
		buf.get(ret,0,size);
		if(!keep) empty();
		//buf.rewind();
		return ret;
	}

	public void empty() {
		mLines.clear();
		this.totalbytes = 0;
		this.brokenLineCount=0;
		appendLast = false;
	}
	
	
	public LinkedList<Line> getLines() {
		return mLines;
	}

	public void setLines(LinkedList<Line> mLines) {
		this.mLines = mLines;
	}

	private static ArrayList<Integer> placeMap = new ArrayList<Integer>();
	
	private static LinkedList<Integer> getOperationsFromBytes(byte[] in) {
		LinkedList<Integer> tmp = new LinkedList<Integer>();
		int working = 0;
		int place = 1;
		placeMap.clear();
		for(int i=0;i<in.length;i++) {
			switch(in[i]) {
			case SEMI:
				//reset 
				
				int finalVal = 0;
				for(int j=0;j<placeMap.size();j++) {
					finalVal += placeMap.get(j) * Math.pow(10, placeMap.size()-1-j);
				}
				placeMap.clear();
				tmp.addLast(new Integer(finalVal));
				working = 0;
				place = 1;
				break;
			case m:
				finalVal = 0;
				for(int j=0;j<placeMap.size();j++) {
					finalVal += placeMap.get(j) * Math.pow(10, placeMap.size()-1-j);
				}
				placeMap.clear();
				
				tmp.addLast(new Integer(finalVal));
				//tmp.addLast(new Integer(working));
				bleedColor = tmp;
				return tmp;
				//end
			case b0:
			case b1:
			case b2:
			case b3:
			case b4:
			case b5:
			case b6:
			case b7:
			case b8:
			case b9:
				placeMap.add(new Integer(getAsciiNumber(in[i])));
				
				working = working*place;
				place = place*10;
				working += getAsciiNumber(in[i]);
				break;
			case ESC:
			case BRACKET:
				break;
			default:
				break;
			}
		}
		
		return tmp;
	}
	
	private static int getAsciiNumber(byte b) {
		switch(b) {
		case b0:
			return 0;
		case b1:
			return 1;
		case b2:
			return 2;
		case b3:
			return 3;
		case b4:
			return 4;
		case b5:
			return 5;
		case b6:
			return 6;
		case b7:
			return 7;
		case b8:
			return 8;
		case b9:
			return 9;
		default:
			return 0;
			
		}
	}
	
	private final byte TAB = (byte)0x09;
	private final static byte ESC = (byte)0x1B;
	private final static byte BRACKET = (byte)0x5B;
	private final byte NEWLINE = (byte)0x0A;
	//private final byte CARRIAGE = (byte)0x0D;
	private final static byte m = (byte)0x6D;
	private final static byte SEMI = (byte)0x3B;
	
	private final static byte b0 = (byte)0x30;
	private final static byte b1 = (byte)0x31;
	private final static byte b2 = (byte)0x32;
	private final static byte b3 = (byte)0x33;
	private final static byte b4 = (byte)0x34;
	private final static byte b5 = (byte)0x35;
	private final static byte b6 = (byte)0x36;
	private final static byte b7 = (byte)0x37;
	private final static byte b8 = (byte)0x38;
	private final static byte b9 = (byte)0x39;
	
	//more ansi escape sequences.
	private final byte A = (byte)0x41;
	private final byte B = (byte)0x42;
	private final byte C = (byte)0x43;
	private final byte D = (byte)0x44;
	private final byte E = (byte)0x45;
	private final byte F = (byte)0x46;
	private final byte G = (byte)0x47;
	private final byte H = (byte)0x48;
	private final byte J = (byte)0x4A;
	private final byte K = (byte)0x4B;
	private final byte S = (byte)0x53;
	private final byte T = (byte)0x54;
	private final byte f = (byte)0x66;
	private final byte s = (byte)0x73;
	private final byte u = (byte)0x75;
	private final byte n = (byte)0x6E;
	
	public void addBytesImplSimple(byte[] data) {
		//int startcount = this.getBrokenLineCount();
		ByteBuffer sb = ByteBuffer.allocate(data.length);
		for(int i=0;i<data.length;i++) {
			if(data[i] == NEWLINE) {
				int size = sb.position();
				byte[] buf = new byte[size];
				sb.rewind();
				sb.get(buf,0,size);
				
				sb.clear();
				
				try {
					Text u = new Text(buf);
					Line l = new Line();
					l.getData().addLast(u);
					l.getData().addLast(new NewLine());
					addLine(l);
				} catch (UnsupportedEncodingException e) {
					
					e.printStackTrace();
				}
				
				
			} else {
				sb.put(data[i]);
			}
		}
		
		if(sb.position() > 0) {
			int size = sb.position();
			byte[] buf = new byte[size];
			sb.rewind();
			sb.get(buf,0,size);
			
			sb.clear();
			
			try {
				Text u = new Text(buf);
				Line l = new Line();
				l.getData().addLast(u);
				addLine(l);
			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
			}
		}
		
		this.prune();
	}
	
	static enum RUN {
		WHITESPACE,
		TEXT,
		NEW
	};
	boolean appendLast = false; //for marking when the addtext call has ended with a newline or not.
	private byte[] holdover = null;
	LinkedList<Integer> prev_color = null;
	Color lastColor = null;
	byte[] strag = null;
	public int addBytesImpl(byte[] data) throws UnsupportedEncodingException {
		//if(simpleMode) {
		//	addBytesImplSimple(data);
		//}
		//this actually shouldn't be too hard to do with just a for loop.
		//STATE init = STATE.TEXT;
		int projected = totalbytes + data.length;
		//Log.e("TREE","ADDING: " + data.length + " bytes, buffer has " + totalbytes + " total bytes. " + projected + " projected.");
		//LinkedList<Line> lines = new LinkedList<Line>();
		
		int startcount = this.getBrokenLineCount();
		int linesadded = 0;
		Line tmp = null;
		
		if(holdover != null) {
			//Log.e("TREE","HOLDOVER SEQUENCE:" + new String(holdover,"ISO-8859-1"));
			ByteBuffer b = ByteBuffer.allocate(holdover.length + data.length);
			b.put(holdover,0,holdover.length);
			b.put(data,0,data.length);
			b.rewind();
			data = b.array();
			holdover = null;
		}
		
		if(mLines.size() > 0) {
			Line analyze = mLines.get(0);
			
			//boolean appendLast = false;
			//Log.e("TREE","ANALYZING: " + deColorLine(analyze));
			
			for(Unit u : analyze.getData()) {
				if(u instanceof Text) {
					appendLast = true;
				} else if(u instanceof NewLine) {
					appendLast = false;
					//linesadded = 1;
				}
			}
			//Log.e("TREE","APPEND LAST IS:" + appendLast);
		}
		
		LinkedList<Unit> ldata = null;
		if(appendLast) { //yay appendLast is over. now just look at the last line of the buffer, parse through it and find if the last text in it (not color) was a newline.
			//if(mLines.size() > 0) {
				tmp = mLines.remove(0); //dont worry kids, it'll be appended back.
				totalbytes -= tmp.bytes; //this will be added back too, this is just to avoid memory leaking
				ldata = tmp.getData();
				brokenLineCount -= tmp.breaks + 1;
				//Log.e("TREE",">>>>>>>>>>>>>>APPENDING TO: " + deColorLine(tmp));
			//}
		} //else {
			//tmp = new Line();
		//}
		
		tmp = new Line();
		
		if(ldata != null) {
			tmp = new Line();
			tmp.setData(ldata);
			//Log.e("TREE","DATA STRIP OUT:" + deColorLine(tmp));
		}
		
		ByteBuffer sb = ByteBuffer.allocate(data.length);
		ByteBuffer cb = ByteBuffer.allocate(data.length);
		RUN runtype = RUN.NEW;
		
		//boolean endOnNewLine = false;
		for(int i=0;i<data.length;i++) {
			//Log.e("TREE","DATA PROCESSING LOOP: " + deColorLine(tmp));
			switch(data[i]) {
			case ESC:
				//Log.e("TREE","BEGIN ANSI ESCAPE");
				//end current text node.
				
				if(sb.position() > 0) {
					int size = sb.position();
					strag = new byte[size];
					sb.rewind();
					sb.get(strag,0,size);
					sb.rewind();
					switch(runtype) {
					case WHITESPACE:
						tmp.getData().addLast(new WhiteSpace(strag));
						break;
					case TEXT:
						tmp.getData().addLast(new Text(strag));
						break;
					default:
						break;
					}
					runtype = RUN.NEW;
					
					
				}
				//text.data = sb.toString();
				//sb.rewind();
				//tmp.getData().addLast(text);
				//text = new Text();
				
				if( (i+1) >= data.length) {
					holdover = new byte[]{ ESC };
					//Log.e("TREE","APPEND DUE TO HOLDOVER EVENT: " + deColorLine(tmp));
					addLine(tmp);
					linesadded += tmp.breaks + 1;
					//tmp = new Line();
					//Log.e("TEXTTREE",getLastTwenty(false));
					
					//Log.e("TREE","HOLDOVER EVENENT, ESC ONLY");
					int endcount = this.getBrokenLineCount();
					//prune();
					return endcount - startcount;
				}
				//start ansi process sequence.
				if(data[i+1] != BRACKET) {
					//invalid ansi sequence.
				}
				cb.put(data[i]);
				cb.put(data[i+1]);
				
				boolean done = false;
				
				if( (i+2) >= data.length) {
					int tmpsize = cb.position();
					holdover = new byte[tmpsize];
					cb.rewind();
					cb.get(holdover,0,tmpsize);
					//Log.e("TREE","APPEND DUE TO HOLDOVER EVENT: " + deColorLine(tmp));
					addLine(tmp);
					linesadded += tmp.breaks + 1;
					//Log.e("TEXTTREE",getLastTwenty(false));
					//Log.e("TREE","HOLDOVER EVENT, ESC AND [");
					int endcount = this.getBrokenLineCount();
					//prune();
					return endcount - startcount;
				}
				
				for(int j=i+2;j<data.length;j++) {
					//Log.e("TREE","ANSI ESCAPE ANALYSIS: " + new String(new byte[]{data[j]}));					
					
					switch(data[j]) {
					case m:
						//Log.e("TREE","STOPPING COLOR PARSE");
						done = true;
						cb.put(m);
						int cmdsize = cb.position();
						byte[] cmd = new byte[cmdsize];
						cb.rewind();
						cb.get(cmd,0,cmdsize);
						
						Color c = new Color(cmd);
						if(lastColor == null) {
							lastColor = c;
							tmp.getData().addLast(c);
						} else if(lastColor.equals(c)) {
							//if(strag != null) {
								//tmp.getData().removeLast();
								//sb.put(strag);	
							//}
							//dont add because the last color is the same.
							if(this.isCullExtraneous()) {
								//do nothing
							} else {
								tmp.getData().addLast(c);
							}
						} else {
							tmp.getData().addLast(c);
							lastColor = c;
						}
						
						cb.rewind();
						break;
					case A:
					case B:
					case C:
					case D:
					case E:
					case F:
					case G:
					case H:
					case J:
					case K:
					case S:
					case T:
					case f:
					case n:
					case s:
					case u:
						done=true;
						cb.rewind();
						break;
					default:
						//Log.e("TREE","APPENDING FOR PARSE");
						cb.put(data[j]);
						break;
					}
					if(done) {
						i = j; //advance the cursor.
						break;
					}
				}
				if(cb.position() > 0) {
					int mtmpsz = cb.position();
					holdover = new byte[mtmpsz];
					cb.rewind();
					cb.get(holdover,0,mtmpsz);
					//Log.e("TREE","APPEND DUE TO UNTERMINATED ANSI SEQUENCE:"  + deColorLine(tmp));
					addLine(tmp);
					linesadded += tmp.breaks + 1;
					//Log.e("TREE","WARNING: UNTERMINATED ASCII SEQUENCE: " + new String(holdover,encoding));
					int endcount = this.getBrokenLineCount();
					//prune();
					return startcount - endcount;
				}
				break;
			case TAB:
				//make new tab node.
				tmp.getData().addLast(new Tab());
				break;
			case NEWLINE:
				//Log.e("TREE","START APPEND DUE TO NEWLINE:"  + deColorLine(tmp));
				//Log.e("TREE","NEWLINE ADDING: " +sb.toString());
				//linesadded += 1;
				if(sb.position() > 0) {
					int nsize = sb.position();
					byte[] txtdata = new byte[nsize];
					sb.rewind();
					sb.get(txtdata,0,nsize);
					//Log.e("TREE","APPEND TO LINE:"  + deColorLine(tmp));
					switch(runtype) {
					case WHITESPACE:
						tmp.getData().addLast(new WhiteSpace(txtdata));
						break;
					case TEXT:
						tmp.getData().addLast(new Text(txtdata));
						break;
					default:
						break;
					}
					runtype = RUN.NEW;
					sb.rewind();
				}
				//append the line as we do.
				NewLine nl = new NewLine();
				tmp.getData().addLast(nl);
				//Log.e("TREE","APPEND DUE TO NEWLINE:"  + deColorLine(tmp));
				addLine(tmp);
				linesadded += tmp.breaks + 1;
				tmp = new Line();
				break;
			default:
				//put it in the buffer.
				if(Character.isWhitespace(data[i])) {
					//start whitespace run
					//Log.e("BYTE","FOUND WHITESPACE");
					switch(runtype) {
					case TEXT:
						int len = sb.position();
						byte[] cap = new byte[len];
						sb.rewind();
						sb.get(cap,0,len);
						tmp.mData.addLast(new Text(cap));
						
						runtype = RUN.WHITESPACE;
						sb.rewind();
						break;
					case NEW:
						runtype = RUN.WHITESPACE;
						break;
					default:
						break;
					}
				} else {
					switch(runtype) {
					case WHITESPACE:
						int len = sb.position();
						byte[] cap = new byte[len];
						sb.rewind();
						sb.get(cap,0,len);
						//Log.e("BYTE","ADDING WHITESPACE RUN");
						tmp.mData.addLast(new WhiteSpace(cap));
						runtype = RUN.TEXT;
						sb.rewind();
						break;
					case NEW:
						runtype = RUN.TEXT;
						break;
					default:
						break;
					}
				}
				sb.put(data[i]);
				//Log.e("TREE","BUFFER NOW:"+sb.toString()+"|");
				//endOnNewLine = false;
				
				break;
			}
		}
		//Log.e("TREE","BUFFER CONTAINS:" +sb.toString() + "||||");
		
		if(sb.position() > 0) {
			//Line last = new Line();
			//last.getData().addLast(new Text(sb.toString()));
			int fsize = sb.position();
			byte[] tmpb = new byte[fsize];
			sb.rewind();
			sb.get(tmpb,0,fsize);
			tmp.getData().addLast(new Text(tmpb));
			//Log.e("TEXTTREE",getLastTwenty(false));
			//Log.e("TEXTTREE","ADDED TEXT: LAST 20 LINES");
			//Log.e("TREE",">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>NOT ENDED BY NEWLINE:" + deColorLine(tmp));
			
			
			sb.rewind();
		}
		
		if(tmp.getData().size() > 0) {
			addLine(tmp);
			linesadded += tmp.breaks + 1;
		}
		
		//if(debugLineAdd) {
		//	Log.e("TREE","ADDED " + linesadded + " LINES TO TREE");
		//}
		
		int endcount = this.getBrokenLineCount();
		prune();
		
		return endcount - startcount;
	}
	
	public void prune() {
		if(mLines.size() > MAX_LINES) {
			while(mLines.size() > MAX_LINES) {
				//Log.e("TREE","TRIMMING BUFFER");
				Line del = mLines.removeLast();
				brokenLineCount -= (1 + del.breaks);
				totalbytes -= del.bytes;
			}
		}
	}
	
	/*private class AddTextHandler extends Handler {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MESSAGE_ADDTEXT:
				try {
					addBytesImpl((byte[])msg.obj);
				} catch (UnsupportedEncodingException e) {
					
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
	}*/
	
	public void updateMetrics() {
		brokenLineCount = 0;
		totalbytes = 0;
		ListIterator<Line> iterator = mLines.listIterator(mLines.size());
		while(iterator.hasPrevious()) {
			Line l = iterator.previous();
			l.updateData();
			totalbytes += l.bytes;
			brokenLineCount += l.breaks + 1;
		}
	}
	
	private void addLine(Line l) {
		l.updateData();
		brokenLineCount += l.breaks + 1;
		totalbytes += l.bytes;
		//Log.e("TREE","A:" + deColorLine(l));
		mLines.add(0,l);
	}
	
	LinkedList<Line> mLines;
	Line pStart;
	Line pSend;
	
	public class Line {
		//protected int totalchars;
		protected int charcount;
		protected int breaks;
		protected int bytes;
		//protected int viswidth;
		private ListIterator<Unit> theIterator = null;
		
		public int getBreaks() {
			return breaks;
		}

		public void setBreaks(int breaks) {
			this.breaks = breaks;
		}

		protected LinkedList<Unit> mData;
		
		public Line() {
			mData = new LinkedList<Unit>();
			breaks =0;
		}
		
		public void updateData() {
			this.breaks = 0;
			this.charcount = 0;
			this.bytes = 0;
			//this.viswidth = 0;
			
			//boolean broken = false;
			//boolean visfound = false;
			//Break lastBreak = null;
			//int backlogvis = 0;
			//int tmpvis = 0;
			
			int charsinline = 0; //tracker for how many characters are in the line
			//int nonWhiteSpaceRun = 0; //tracker for how many characters have accumulated without whitespace
			boolean whiteSpaceFound = false;
			
			theIterator = mData.listIterator(0);
			stripBreaks();
			//Counter counter = new Counter();
			while(theIterator.hasPrevious()) {
				theIterator.previous();
			}
			
			while(theIterator.hasNext()) {
			//while()
			
				Unit u = theIterator.next();
				
				switch(u.type) {
				case WHITESPACE:
					if(wordWrap) whiteSpaceFound = true;
				case TEXT:
					charsinline += ((Text)u).charcount;
					this.bytes += ((Text)u).bytecount;
					this.charcount += u.charcount;
					break;
				case TAB:
				case NEWLINE:
				case COLOR:
					this.bytes += u.reportSize();
					break;
				case BREAK:
					theIterator.remove();
					this.breaks -= 1;
					break;	
				}
				//check if it is whitespace
//				if(u instanceof WhiteSpace) {
//					if(wordWrap) {
//						whiteSpaceFound = true;
//					}
//					//this.charcount += u.charcount;
//				}
//				
//				if(u instanceof Text) {
//					//update charsinline
//					charsinline += ((Text)u).charcount;
//					this.bytes += ((Text)u).charcount;
//					this.charcount += u.charcount;
//				}
//				
//				if(u instanceof Tab || u instanceof NewLine || u instanceof Color) {
//					this.bytes += u.reportSize();
//				}
//				if(u instanceof Break) {
//					theIterator.remove();
//					this.breaks -= 1;
//				}
				
				if(charsinline > breakAt) {
					int amount = charsinline - breakAt;
					if(wordWrap) {
						if(whiteSpaceFound) {
							//find the nearest whitespace and break.
							boolean found = false;
							//i.previous(); //advance back because we are on the right hand side of the unit that broke.
							while(!found && theIterator.hasPrevious()) {
								Unit tmp = theIterator.previous();
								if(tmp instanceof WhiteSpace) {
									theIterator.next(); //get on the right side of the unit.
									Break b = new Break();
									theIterator.add(b);
									this.breaks += 1;
									found = true;
									
								} 
								
							}
							
							whiteSpaceFound = false;
							charsinline = 0;
						
						} else {
							//just break here and continue
							//if(amount > u.charcount) {
							//	Log.e("TREE","INVESTIGATE ME");
							//}
							int pos = u.charcount - (u.charcount-amount);
							pos += 1;
							pos -= 1;
							breakAt(theIterator,u,pos,u.charcount);
							charsinline = 0;
						}
						
					//if the number of non whitespace characters is < breakAt, then we should go back and search for the whitespace
					//else, break in the middle.
					} else {
						//just break in the middle as we are not word wrapping
						//charsinline = breakAt(theIterator,u,amount,u.charcount);
						breakAt(theIterator,u,amount,u.charcount);
						charsinline = 0;
					}
				}
				
			}
			
			
			
			while(theIterator.hasPrevious()) {
				theIterator.previous();
			}
			
			//if we are here, then we should work backward through the list requesting sizes
			//this.bytes = 0;
			//while(i.hasPrevious()) {
			//	Unit tmp = i.previous();
			//	this.bytes += tmp.reportSize();
			//}
			
			//Log.e("TREE",this.bytes + ":"+deColorLine(this));
			
		}
		
		public ListIterator<Unit> getIterator() {
			return theIterator;
		}
		
		public void resetIterator() {
			while(theIterator.hasPrevious()) {
				theIterator.previous();
			}
		}
		
		public void stripBreaks() {
			//Iterator<Unit> stripper = mData.iterator();
			while(theIterator.hasPrevious()) {
				theIterator.previous();
			}
			while(theIterator.hasNext()) {
				Unit tmp = theIterator.next();
				if(tmp instanceof Break) {
					theIterator.remove();
				}
			}
		}

		public final Text newText(String str) {
			return new Text(str);
		}
		
		/*public final Color newColor(int c) { //constructs a new xterm256 color.
			//byte[] x = new byte[6];
			//x[0] = ESC;
			//x[1] = BRACKET;
			//x[2] = 38
		}*/
		
		public Color newColor(int color)
		{
			Color c = new Color();
			c.operations.add(38);
			c.operations.add(5);
			c.operations.add(color);
			c.bytecount = 1 + 1 + 2 + 1 + 1 + 1 + (Integer.toString(color)).length() + 1;
			//           ESC  [  38   ;   5   ;   color data, can be up to 3           m
			String foo = null;
			try {
				foo = new String(new byte[]{ESC},encoding) + "[38;5;"+color+"m";
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				c.bin = foo.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return c;
		}
		
		public Color newBackgroundColor(int color)
		{
			Color c = new Color();
			c.operations.add(48);
			c.operations.add(5);
			c.operations.add(color);
			c.bytecount = 1 + 1 + 2 + 1 + 1 + 1 + (Integer.toString(color)).length() + 1;
			//           ESC  [  38   ;   5   ;   color data, can be up to 3           m
			String foo = null;
			try {
				foo = new String(new byte[]{ESC},encoding) + "[48;5;"+color+"m";
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				c.bin = foo.getBytes(encoding);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return c;
		}
		
		private int breakAt(ListIterator<Unit> i, Unit u, int amount, int length) {
			int charsinline;
			boolean removed;
			if(amount == 0) {
				i.add(new Break());
				//advance so we don't process this for the original break checking.
				if(i.hasNext()) {
					i.next(); //advance the cursor so we don't go through and delete existing breaks.
				}
				breaks += 1;
				charsinline = 0;
				removed = true;
			} else {
				int start = length - amount;
				int end = length - (length-amount);
				
				try {
				String first = ((Text)u).data.substring(0, start);
				String second = ((Text)u).data.substring(start,start+end);
				i.set(new Text(first));
				i.add(new Break());
				i.add(new Text(second));
				} catch (StringIndexOutOfBoundsException e) { 
					throw e;
				}
				
				//length = end;
				breaks += 1;
				
				
				removed = true;
				
				charsinline = end;
			}
			
			if(removed) {
				i.previous(); //queue the next pass to start with the unbroken end
				//Iterator<Unit> ja = this.mData.iterator();
				//StringBuilder b = new StringBuilder();
				//while(ja.hasNext()) {
				//	Unit tz = ja.next();
				//	if(tz instanceof Text) {
				//		b.append(((Text)tz).getString());
				//	}
				//	if(tz instanceof Break) {
				//		b.append("|");
				//	}
				//}
				//Log.e("TREE","BROKE LINE: " + b.toString());
				//break;
			}
			return charsinline;
		}

		public LinkedList<Unit> getData() {
			return mData;
		}
		
		public void setData(LinkedList<Unit> l) {
			mData = l;
			//need to parse this to make sure we report the correct data.
			charcount = 0;
			//totalchars = 0;
			breaks = 0;
			for(Unit u : mData) {
				if(u instanceof Text) {
					charcount += u.charcount;
					///totalchars += u.charcount;
				}
				if(u instanceof Color) {
					//totalchars += u.charcount;
				}
			}
			
			theIterator = null;
			theIterator = mData.listIterator(0);
		}
		
		
		
	}
	
	public enum UNIT_TYPE {
		BLAND,
		TEXT,
		WHITESPACE,
		TAB,
		NEWLINE,
		COLOR,
		BREAK,
	}
	
	public class Unit {
		protected int charcount;
		protected int bytecount;
		//protected int bytecount;
		
		public Unit() { charcount = 0; bytecount=0; }
		//	charcount = 0;
		//}
		public UNIT_TYPE type = UNIT_TYPE.BLAND;
		
		//public Unit copy() { return null;}
		public int reportSize() { return 0; } //raw units have no size.
		
	}
	
	
	public class Text extends Unit {
		protected String data;
		protected byte[] bin;
		private boolean link = false;
		
		//public Text copy() {
		//	return null;
		//}
		public Text() {
			data = "";
			charcount = 0;
			bytecount = 0;
			bin = new byte[0];
			this.type = UNIT_TYPE.TEXT;
		}
		
		public Text(String input) {
			
			if(linkify && (input.length() > 4)) {
				urlMatcher.reset(input);
				if(urlMatcher.find()) {
					this.link = true;
				}
			}
			
			data = input;
			this.charcount = data.length();
			try {
				bin = data.getBytes(encoding);
				this.bytecount = bin.length;
			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
			}
			
			this.type = UNIT_TYPE.TEXT;
		}
		
		public Text(byte[] in) throws UnsupportedEncodingException {
			bin = in;
			data = new String(in,encoding);
			if(linkify && in.length > 4) {
				urlMatcher.reset(data);
				if(urlMatcher.find()) {
					this.link = true;
				}
			}
			this.charcount = data.length();
			bytecount = bin.length;
			this.type = UNIT_TYPE.TEXT;
		}

		public String getString() {
			return data;
		}
		
		public byte[] getBytes() {
			return bin;
		}
		
		public int reportSize() {
		
			return bin.length;
			
		}

		public void setLink(boolean link) {
			this.link = link;
		}

		public boolean isLink() {
			return link;
		}
		
		//public Text copy() {
			
			
		//}
		
		
	}
	
	
	
	private class Tab extends Unit {
		//protected String data;
		
		public Tab() {
			//data = new String(new byte[]{0x09});
			this.charcount = 1;
			this.bytecount = 1;
			this.type = UNIT_TYPE.TAB;
		}
		
		public int reportSize() {
			return 1;
		}
		
	}
	public class NewLine extends Unit {
		protected String data;
		
		public NewLine() {
			data = new String("\n");
			this.charcount = 1;
			this.bytecount = 1;
			this.type = UNIT_TYPE.NEWLINE;
		}
		
		public int reportSize() {
			return 1;
		}
	}
	
	/*public class Counter extends Unit {
		public int count = 0;
		public Counter() {
			
		}
		
		public int reportSize() {
			return 0;
		}
	}*/
	
	
	public class Color extends Unit {
		protected byte[] bin;
		//protected String data;
		ArrayList<Integer> operations;
		//ListIterator<Integer> it;
		
		public Color() {
			//data = "[0m";
			//this.charcount = data.length();
			operations = new ArrayList<Integer>();
			//operations.add(new Integer(0));
			this.type = UNIT_TYPE.COLOR;
		}
		
		public void setOperations(ArrayList<Integer> ops) {
			this.operations = ops;
		}
		//public Color(String input) {
			//data = input;
			//this.charcount = data.length();
		//	computeOperations(input);
			//try {
				//bytecount = data.getBytes(encoding).length;
			//} catch (UnsupportedEncodingException e) {
			//	
		//		e.printStackTrace();
		//	}
		//}
		
		/*public Color(String input,LinkedList<Integer> ops) {
			data = input;
			this.charcount = data.length();
			operations = new ArrayList<Integer>(ops); //will need to track this for actual memory usage.
			try {
				bytecount = data.getBytes(encoding).length;
			} catch (UnsupportedEncodingException e) {
		
				e.printStackTrace();
			}
		}*/
		
		public Color(byte[] input) {
			bin = input;
			bytecount = input.length;
			operations = new ArrayList<Integer>(getOperationsFromBytes(input));
			//it = operations.listIterator();
			/*try {
				data = new String(bin,encoding);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}*/
			this.type = UNIT_TYPE.COLOR;
			
		}
		
		//public String getData() {
		//	return data;
		//}
		
		public void computeOperations(String input) {
			//
		}

		public ArrayList<Integer> getOperations() {
			return operations;
		}
		
		public boolean equals(Object o) {
			if(o == this) return true;
			if(!(o instanceof Color)) return false;
			Color c = (Color)o;
			if(c.bin.length != this.bin.length) {
				return false;
			}
			for(int i=0;i<this.bin.length;i++) {
				if(c.bin[i] != this.bin[i]) {
					return false;
				}
			}
			
			return true;
		}
		
		public int reportSize() {
			return bin.length;
		}
	}
	
	public class Break extends Unit {
		public int viswidth = 0;
		public Break() {
			this.type = UNIT_TYPE.BREAK;
		}
		public int reportSize() {
			return 0;
		}
		
	}
	
	public class WhiteSpace extends Text {
		//whitespace is esentially text.
		public WhiteSpace() {
			super();
			this.type = UNIT_TYPE.WHITESPACE;
		}
		
		public WhiteSpace(String pIn) {
			super(pIn);
			this.type = UNIT_TYPE.WHITESPACE;
		}
		
		public WhiteSpace(byte[] pIn) throws UnsupportedEncodingException {
			super(pIn);
			this.type = UNIT_TYPE.WHITESPACE;
		}
		
		public String getString() {
			return data;
		}
		
		public byte[] getBytes() {
			return bin;
		}
		
		public int reportSize() {
			return super.reportSize();
		}
	}
	
	public String getLastTwenty(boolean showcolor) {
		StringBuffer buf = new StringBuffer();
		Iterator<Line> i = mLines.iterator();
		int j = 0;
		while(j < 20) {
			if(i.hasNext()) {
				buf.insert(0,j + ":" + deColorLine((Line)i.next()));
				//buf.insert(0,"\n");
				//
			}
			j++;
		}
		return buf.toString();
	}
	
	private static StringBuffer stripColor = new StringBuffer();
	public static StringBuffer deColorLine(Line line) {
		stripColor.setLength(0);
		for(Unit u : line.getData()) {
			if(u instanceof Text) {
				stripColor.append(((Text)u).data);
			}
			
			//if(u instanceof NewLine) {
				//stripColor.append("\n");
			//}
			
		}
		
		return stripColor;
	}

	public void setMaxLines(int maxLines) {
		MAX_LINES = maxLines;
	}

	public void setLineBreakAt(Integer i) {
		breakAt = i;
		updateTree();
	}
	
	private void updateTree() {
		brokenLineCount = 0;
		totalbytes = 0;
		for(Line l : mLines) {
			l.updateData();
			totalbytes += l.bytes;
			brokenLineCount += (1 + l.breaks);
		}
	}

	public void setWordWrap(boolean wordWrap) {
		boolean doupdate = false;
		if(wordWrap != this.wordWrap) doupdate = true;
		this.wordWrap = wordWrap;
		if(doupdate) {
			updateTree();
		}
	}

	public boolean isWordWrap() {
		return wordWrap;
	}

	public void setCullExtraneous(boolean cullExtraneous) {
		this.cullExtraneous = cullExtraneous;
	}

	public boolean isCullExtraneous() {
		return cullExtraneous;
	}

	public void setLinkify(boolean linkify) {
		this.linkify = linkify;
	}

	public boolean isLinkify() {
		return linkify;
	}

	public void setBleedColor(Color c) {
		bleedColor = new LinkedList<Integer>(c.getOperations());
	}

	public Color getBleedColor() {
		Color c = new Color();
		c.setOperations(new ArrayList<Integer>((LinkedList<Integer>)bleedColor.clone()));
		StringBuffer b = new StringBuffer();
		try {
			b.append(new String(new byte[]{ESC},encoding));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		b.append("[");
		Iterator<Integer> it = c.getOperations().iterator();
		while(it.hasNext()) {
			Integer i = it.next();
			b.append(i);
			if(it.hasNext()) {
				b.append(";");
			} else {
				b.append("m");
			}
			
			
		}
		try {
			c.bin = b.toString().getBytes(encoding);
			c.bytecount = c.bin.length;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		b.setLength(0);
		b = null;
		//c.bin = (ESC + "[" )
		
		return c;
	}

	public void appendLine(Line line) {
		addLine(line);
		
	}

	public Selection getSelectionForPoint(int line, int column) {
		//this is neat.
		//get an iterator.
		Line data = null;
		ListIterator<Line> lineIterator = mLines.listIterator();
		boolean done = false;
		int working = 0;
		int subline = 0;
		//if(line == 0) {
		while(lineIterator.hasNext() && !done) {
			
			Line l = lineIterator.next();
			
			
			if(l.breaks > 0) {
				subline = l.breaks;
				
				for(int i=0;i<l.breaks;i++) {
					if(i != 0) {subline = subline - 1;};
					if(working == line) {
						data = l;
						done = true;
						i=l.breaks;
					}
					working += 1;
					
				}
				
				if(!done) {
					subline = 0;
					if(working == line){
						data = l;
						done = true;
					}
					working += 1;
				}
				
				
			} else {
				
				if(working == line) {
					data = l;
					done = true;
				}
				working += 1;
			}
		}
		
		if(!done) {
			//no text there.
			return null;
		}
		
		
		if(data.bytes == 0) {
			return null;
		}
		//i.getIterator().r
		ListIterator<Unit> i = data.getData().listIterator();
		
		//advance to the subline.
		done = false;
		working = 0;
		if(subline > 0) {
			while(i.hasNext() && !done) {
				Unit u = i.next();
				if(u instanceof Break) {
					working += 1;
					if(working == subline) {
						done = true;
					}
				}
			}
		}
		
		done = false;
		working = 0;
		while(i.hasNext() && !done) {
			Unit u = i.next();
			if(u instanceof WhiteSpace) {
				working += u.bytecount;
				if(working >= column) {
					//get the text on either side.
					boolean subdone = false;
					int startline,startcol,endline,endcol;
					Unit prevUnit = null;
					int unitsback = 1;
					i.previous();
					startline = line;
					startcol = working - u.bytecount;
					endline = line;
					//StringBuilder output = new StringBuilder();
					while(!subdone && i.hasPrevious()) {
						Unit subu = i.previous();
						if(subu instanceof Text) {
							prevUnit = subu;
							subdone = true;
							//output.append(((Text)subu).getString());
							startcol = startcol - ((Text)subu).bytecount;
						}
						unitsback += 1;
					}
					endcol = startcol;
					for(int j=0;j<unitsback;j++) {
						Unit tmp = i.next();
						if(tmp instanceof Text) {
							//output.append(((Text)tmp).getString());
							endcol += ((Text)u).bytecount;
						}
					}					
					subdone = false;
					while(!subdone && i.hasNext()) {
						Unit tmp = i.next();
						if(tmp instanceof Text) {
							//output.append(((Text)tmp).getString());
							endcol += ((Text)u).bytecount;
							SelectionCursor start = new SelectionCursor(startline,startcol);
							SelectionCursor end = new SelectionCursor(endline, endcol);
							Selection selection = new Selection(start, end);
							return selection;
						}
					}
					
				}
			} else if(u instanceof Text) {
				working += u.bytecount;
				if(working >= column) {
					int startline,startcol,endline,endcol;
					startline = line;
					endline = line;
					startcol = working - u.bytecount;
					//find the next whitespace.
					int units_back = 1;
					i.previous();
					boolean subdone = false;
					while(!subdone && i.hasPrevious()) {
						Unit tmp = i.previous();
						if(tmp instanceof WhiteSpace) {
							subdone = true;
						} else if(tmp instanceof Text) {
							startcol = startcol - ((Text)tmp).bytecount;
						}
						units_back += 1;
					}
					endcol = startcol-1;
					i.next();
					for(int j=0;j<units_back-1;j++) {
						Unit tmp = i.next();
						if(tmp instanceof Text) {
							endcol += ((Text)tmp).bytecount;
						}
					}
					subdone = false;
					while(!subdone && i.hasNext()) {
						Unit tmp = i.next();
						if(tmp instanceof WhiteSpace) {
							subdone = true;
						} else if(tmp instanceof Text) {
							endcol += ((Text)tmp).bytecount;
						}
					}
					
					SelectionCursor start = new SelectionCursor(startline,startcol);
					SelectionCursor end = new SelectionCursor(endline, endcol);
					Selection selection = new Selection(start, end);
					return selection;
					//return ((Text)u).getString();
				}
			}
		}
		
		return null;
	}
	
	public String getTextSection(Selection selection) {
		
		int startline,startcol,endline,endcol;
		
		if(selection.end.line > selection.start.line) {
			
			startline = selection.end.line;
			startcol = selection.end.column;
			endline = selection.start.line;
			endcol = selection.start.column;
		} else {
			startline = selection.start.line;
			startcol = selection.start.column;
			endline = selection.end.line;
			endcol = selection.end.column;
		}
		
		StringBuilder builder = new StringBuilder();
		
		boolean startfound = false;
		ListIterator<Line> lineIterator = mLines.listIterator();
		Line firstdata = null;
		//boolean done = false;
		int working = 0;
		int subline = 0;
		int workingLine = 0;
		//if(line == 0) {
		while(lineIterator.hasNext() && !startfound) {
			
			Line l = lineIterator.next();
			
			
			if(l.breaks > 0) {
				subline = l.breaks;
				
				for(int i=0;i<l.breaks;i++) {
					if(i != 0) {subline = subline - 1;};
					if(working == startline) {
						firstdata = l;
						startfound = true;
						i=l.breaks;
					}
					working += 1;
					
				}
				
				if(!startfound) {
					subline = 0;
					if(working == startline){
						firstdata = l;
						startfound = true;
					}
					working += 1;
				}
				
				
			} else {
				
				if(working == startline) {
					firstdata = l;
					startfound = true;
				}
				working += 1;
			}
		}
		workingLine = startline;
		if(!startfound) {
			//no text there.
			return null;
		}
		
		lineIterator.previous();
		
		boolean done = false;
		ListIterator<Unit> li = firstdata.getData().listIterator();
		working = 0;
		if(subline > 0) {
			while(li.hasNext() && !done) {
				Unit u = li.next();
				if(u instanceof Break) {
					working += 1;
					if(working == subline) {
						done = true;
					}
				}
			}
			

		}
		
		done = false;
		working = 0;
		while(!done && li.hasNext()) {
			Unit u = li.next();
			if(u instanceof Text) {
				working += ((Text)u).bytecount;
				if(working >= startcol) {
					
					builder.append(((Text)u).getString().substring(((Text)u).bytecount-(working-startcol), ((Text)u).bytecount));
					
					done = true;
					
				}
			}
		}
		
		if(startline == endline || startline - endline < (firstdata.breaks+1)) {
			done = false;
			while(li.hasNext() && !done) {
				Unit u = li.next();
				done = false;
				if(u instanceof Text) {
					working += ((Text)u).bytecount;
					if(workingLine == endline && working > endcol) {
						if(((Text)u).bytecount == 1) {
							builder.append(((Text)u).getString());
						} else {
							builder.append(((Text)u).getString().substring(0,endcol-(working-((Text)u).bytecount-1)));
						}
						done = true;
					} else {
						builder.append(((Text)u).getString());
					}
				}
				if(u instanceof Break) {
					working = 0;
					workingLine -= 1;
				}
				
			}
			return builder.toString();
		} else {
			done = false;
			while(li.hasNext()) {
				Unit u = li.next();
				if(u instanceof Text) {
					builder.append(((Text)u).getString());
				} else if(u instanceof Break) {
					working = 0;
					workingLine -=1;
				}
				
			}
			builder.append("\n");
			done = false;
			while(!done && lineIterator.hasPrevious()) {
				Line tmp = lineIterator.previous();
				workingLine -= 1 + tmp.breaks;
				if(workingLine <= endline) {
					workingLine = workingLine + tmp.getBreaks();
					Iterator<Unit> slow = tmp.getData().iterator();
					boolean enddone = false;
					working = 0;
					while(!enddone && slow.hasNext()) {
						Unit u = slow.next();
						if(u instanceof Text) {
							working += ((Text)u).bytecount;
							if(workingLine == endline && working > endcol) {
								if(((Text)u).bytecount == 1) {
									builder.append((((Text)u).getString()));
								} else{
									builder.append(((Text)u).getString().substring(0,endcol-(working-((Text)u).bytecount-1)));
								}
								enddone = true;
								done = true;
							} else {
								builder.append(((Text)u).getString());
							}
						} if(u instanceof Break) {
							working = 0;
							workingLine -= 1;
						}
					}
					done = true;
				} else {
					Iterator<Unit> fast = tmp.getData().iterator();
					while(fast.hasNext()) {
						Unit u = fast.next();
						if(u instanceof Text) {
							builder.append(((Text)u).getString());
						}
					}
					builder.append("\n");
				}
			}
			return builder.toString();
		}
		
		
		
		//return null;
	}
	
	public int getModCount() {
		return modCount;
	}

	public void setModCount(int modCount) {
		this.modCount = modCount;
	}

	public class SelectionCursor {
		public int line,column;
		
		public SelectionCursor(int line,int column) {
			this.line = line;
			this.column = column;
		}
	}
	
	public class Selection {
		SelectionCursor start,end;
		
		public Selection(SelectionCursor start,SelectionCursor end) {
			this.start = start;
			this.end = end;
		}
		
	}
	
	/*public Line makeLine(String str) {
		Line l = new Line();
		LinkedList<Unit> tmp = new LinkedList<Unit>();
		Text t = l.newText(str);
		tmp.add(t);
		l.setData(tmp);
		return l;
	}*/
}
