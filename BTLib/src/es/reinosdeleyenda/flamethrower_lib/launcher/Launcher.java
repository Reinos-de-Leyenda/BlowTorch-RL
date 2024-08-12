package es.reinosdeleyenda.flamethrower_lib.launcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.format.Time;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TimeFormatException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//import android.support.v4.app.ActivityCompat;
import android.support.design.widget.Snackbar;



import es.reinosdeleyenda.flamethrower_lib.R;
import es.reinosdeleyenda.flamethrower_lib.service.IConnectionBinder;
import es.reinosdeleyenda.flamethrower_lib.service.ILauncherCallback;
import es.reinosdeleyenda.flamethrower_lib.service.StellarService;
import es.reinosdeleyenda.flamethrower_lib.settings.ConfigurationLoader;
import es.reinosdeleyenda.flamethrower_lib.ui.SDCardUtils;


public class Launcher extends AppCompatActivity implements ReadyListener,ActivityCompat.OnRequestPermissionsResultCallback {
	
	public static final String PREFS_NAME = "CONDIALOG_SETTINGS";
	
	private Pattern xmlinsensitive = Pattern.compile("^.+\\.[Xx][Mm][Ll]$");
	private Matcher xmlimatcher = xmlinsensitive.matcher("");

	protected static final int MESSAGE_WHATSNEW = 1;
	protected static final int MESSAGE_IMPORT = 2;
	protected static final int MESSAGE_EXPORT = 3;

	protected static final int MESSAGE_USERNAME = 4;

	protected static final int MESSAGE_DORECOVERY = 5;

	protected static final int RP_INFO = 100;
	protected static final int RP_SALVAGE = 101;
	protected static final int RP_EXPORT = 102;
	protected static final int RP_IMPORT = 103;
	
	private IConnectionBinder service = null;
	
	private ArrayList<MudConnection> connections;
	private Launcher.ConnectionAdapter apdapter;
	
	ListView lv = null;
	
	Handler actionHandler;
	
	LauncherSettings launcher_settings;
	
	//IConnectionBinder service;
	
	/*public enum LAUNCH_MODE {
		FREE,
		PAID,
		TEST
	}*/
	
	//private LAUNCH_MODE mode = LAUNCH_MODE.FREE;
	private String launcher_source = "";
	
	//make this save a change
	boolean dowhatsnew = false;

	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		fixClassLoaderIssue();
		//Log.e("LAUNCHER","Launched from package: " + this.getPackageName());
		//determine launch mode
		//Intent intent = this.getIntent();



		
		
		launcher_source = this.getIntent().getStringExtra("LAUNCH_MODE");
		if(launcher_source == null) {
			//Log.e("BlowTorch","Launcher not provided a valid launch source. Finishing.");
			this.finish();
		}
		
		/*if(launcher_source.equals("es.reinosdeleyenda.flamethrowertest")) {
			mode = LAUNCH_MODE.TEST;
			//Log.e("BlowTorch","Test Launcher Engaged.");
		} else if(launcher_source.equals("com.happygoatstudios.flamethrower_lib")) {
			//Log.e("BlowTorch","Free Launcher Engaged.");
			mode = LAUNCH_MODE.FREE;
		} else if(launcher_source.equals("es.reinosdeleyenda.flamethrowerpro")) {
			//Log.e("BlowTorch","Paid Launcher Engaged");
			mode = LAUNCH_MODE.PAID;
		} else {
			//Log.e("BlowTorch","Launcher given source: " + launcher_source + " which is invalid, Finishing");
			this.finish();
		}*/
		
		actionHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch(msg.what) {
				case MESSAGE_USERNAME:
					SharedPreferences.Editor edit = Launcher.this.getSharedPreferences("TEST_USER", Context.MODE_PRIVATE).edit();
					edit.putString("USER_NAME", (String)msg.obj);
					edit.commit();
					break;
				case MESSAGE_WHATSNEW:
					break;
				case MESSAGE_IMPORT:

					//if the file exists, we will get here, if not, it will go to file not found.
					try {
						LauncherSAXParser parser = new LauncherSAXParser((String)msg.obj,Launcher.this);
						launcher_settings = parser.load();
					} catch (RuntimeException e) {
						AlertDialog.Builder error = new AlertDialog.Builder(Launcher.this);
						error.setTitle("Error loading XML");
						error.setMessage(e.getMessage());
						error.setPositiveButton("Acknowledge.",new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						});
						AlertDialog errordialog = error.create();
						errordialog.show();
						return;
					}
					//update this list to the new version.
					PackageManager m = Launcher.this.getPackageManager();
					String versionString = null;
					try {
						versionString = m.getPackageInfo(Launcher.this.getApplicationInfo().packageName, PackageManager.GET_CONFIGURATIONS).versionName;
					} catch (NameNotFoundException e) {
						//can't execute on our package aye?
						throw new RuntimeException(e);
					}
					launcher_settings.setCurrentVersion(versionString);
					buildList();
					saveXML();
					break;
				case MESSAGE_EXPORT:

					break;
				case MESSAGE_DORECOVERY:

					break;
				default:
					break;
				}
			}
		};
		
		setContentView(R.layout.new_launcher_layout);
		android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.my_toolbar);
		//myToolbar.set
		setSupportActionBar(myToolbar);
		int testversion = 0;
		//if(mode == LAUNCH_MODE.TEST) {
		if(ConfigurationLoader.isTestMode(this)) {
			findViewById(R.id.test_update).setVisibility(View.VISIBLE);
			try {
				//this.getPackageManager().getPackageInfo(launcher_source, PackageManager.GET_META_DATA).;
				ApplicationInfo testLauncher = this.getPackageManager().getApplicationInfo(launcher_source, PackageManager.GET_META_DATA);
				if(testLauncher != null) {
					if(testLauncher.metaData != null) {
						testversion = testLauncher.metaData.getInt("BLOWTORCH_TEST_VERSION");
					} else {
						//Log.e("BlowTorch","metaData is null");
						return;
					}
				
				} else {
					//Log.e("BlowTorch","ApplicationInfo is null");
					return;
				}
				//int testversion = this.getPackageManager().getApplicationInfo(launcher_source, PackageManager.GET_META_DATA).metaData.getInt("TEST_VERSION");
				((TextView)findViewById(R.id.update_label)).setText("Test Version " + testversion);
				
				boolean needsupdate = true;
				
				
				
				if(needsupdate) {
					//check if file exists.
					findViewById(R.id.update_button).setVisibility(View.VISIBLE);
					findViewById(R.id.update_button).setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View v) {
							update = new UpdateThread(updateHandler);
							update.start();
							//
//							updateDialog = ProgressDialog.show(Launcher.this, "", "Checking update status",true,true,new DialogInterface.OnCancelListener() {
//								
//								//@Override
//								public void onCancel(DialogInterface dialog) {
//									return;
//								}
//							});
//							URL url2 = null;
//							try {
//								url2 = new URL("http://bt.happygoatstudios.com/test/version");
//							} catch (MalformedURLException e1) {
//								// TODO Auto-generated catch block
//								e1.printStackTrace();
//							}
//							try {
//								BufferedReader in = new BufferedReader(new InputStreamReader(url2.openStream()));
//								StringBuffer buf = new StringBuffer();
//								String tmp;
//								while((tmp = in.readLine()) != null) {
//									buf.append(tmp);
//								}
//								try {
//									Integer newVersion = Integer.parseInt(buf.toString());
//									//Log.e("BlowTorch","Web update version: " + newVersion);
//									ApplicationInfo testLauncher = Launcher.this.getPackageManager().getApplicationInfo(launcher_source, PackageManager.GET_META_DATA);
//									int testversionName = testLauncher.metaData.getInt("BLOWTORCH_TEST_VERSION");
//									int testversion = newVersion;
//									PackageManager pm = Launcher.this.getPackageManager();
//									testversion = pm.getPackageInfo(testLauncher.packageName, PackageManager.GET_CONFIGURATIONS).versionCode;
//									if(newVersion > testversion) {
//										//needsupdate = true;
//									} else {
//										Toast t = Toast.makeText(Launcher.this, "BlowTorch Test Version "+testversionName+" is up to date.", Toast.LENGTH_SHORT);
//										t.show();
//										updateDialog.dismiss();
//										updateDialog = null;
//										return;
//									}
//								} catch(NumberFormatException e) {
//								} catch (NameNotFoundException e) {
//									e.printStackTrace();
//								}
//								
//							} catch (IOException e) {
//								e.printStackTrace();
//							}
//							updateDialog.dismiss();
//							updateDialog = null;
//							
//							String state = Environment.getExternalStorageState();
//							if(!state.equals(Environment.MEDIA_MOUNTED)) {
//								//no sd card
//							} else {
//								
//								updateDialog = ProgressDialog.show(Launcher.this, "", "Downloading update.",true,true,new DialogInterface.OnCancelListener() {
//									
//									public void onCancel(DialogInterface dialog) {
//										
//									}
//								});
//								updateHandler.sendEmptyMessageDelayed(MESSAGE_STARTDOWNLOAD,1000);
//								
//							}
						}
					});
					
					
					
				}
				
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			//}
		}
		
		
		
		launcher_settings = new LauncherSettings();
		connections = new ArrayList<MudConnection>();
		
		lv = (ListView)findViewById(R.id.connection_list);
		apdapter = new ConnectionAdapter(lv.getContext(),R.layout.connection_row,connections);
		
		
		lv.setAdapter(apdapter);
		
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new listItemClicked());
		lv.setOnItemLongClickListener(new listItemLongClicked());
		
		
		lv.setEmptyView(findViewById(R.id.launcher_empty));
		
		try { 
			FileInputStream fos = this.openFileInput("blowtorch_launcher_list.xml");
			fos.close();
			LauncherSAXParser parser = new LauncherSAXParser("blowtorch_launcher_list.xml",this);
			launcher_settings = parser.load();
			if(launcher_settings == null) {
				launcher_settings = new LauncherSettings();
				String[] files = this.fileList();
				for(String file : files) {
					Log.e("BLOWTORCH","Internal settings: " + file);
				}
			}
			//buildList();
			//Log.e("LAUNCHER","LOADING XML LAUNCHER");
		} catch (FileNotFoundException e) {
			//attempt to read the connections from disk.
			//Log.e("LAUNCHER","LOADING CRAPPY LAUNCHER");
			getConnectionsFromDisk();
			//fill the new settings
			int size = apdapter.getCount();
			Time t = new Time();
			t.set(System.currentTimeMillis());
			long starttime = System.currentTimeMillis();
			for(int i=0;i<size;i++) {
				MudConnection tmp = apdapter.getItem(i);
				Time oldertime = new Time();
				oldertime.set(starttime - 1000*i);
				tmp.setLastPlayed(oldertime.format2445());
				launcher_settings.getList().put(tmp.getDisplayName(), tmp.copy());
				
			}
			
			//get the version information.
			//PackageManager m = this.getPackageManager();
			//String versionString = null;
			//try {
			//	versionString = m.getPackageInfo("com.happygoatstudios.flamethrower_lib", PackageManager.GET_CONFIGURATIONS).versionName;
			//} catch (NameNotFoundException e1) {
				//can't execute on our package aye?
			//	throw new RuntimeException(e);
			//}
			
			//Log.e("LAUNCHER","LOADING OLD SETTINGS AND MARKING VERSION: " + versionString);
			launcher_settings.setCurrentVersion("1.0.4");
			
			saveXML();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		//by here we should have a completly populated list and settings
		//check version code.
		PackageManager m = this.getPackageManager();
		String versionString = null;
		try {
			versionString = m.getPackageInfo(launcher_source, PackageManager.GET_CONFIGURATIONS).versionName;
		} catch (NameNotFoundException e) {
			//can't execute on our package aye?
			throw new RuntimeException(e);
		}
		
		int now_major = 1;
		int now_minor = 0;
		int now_rev = 0;
		
		int prev_major = 1;
		int prev_minor = 0;
		int prev_rev = 0;
		//compare version codes.
		Pattern version = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)$");
		Matcher vmatch = version.matcher(versionString);
		if(vmatch.matches()) {
			now_major = Integer.parseInt(vmatch.group(1));
			now_minor = Integer.parseInt(vmatch.group(2));
			now_rev = Integer.parseInt(vmatch.group(3));
		} else {
			//shouldn't really happen.
		}
		
		vmatch.reset(launcher_settings.getCurrentVersion());
		if(vmatch.matches()) {
			prev_major = Integer.parseInt(vmatch.group(1));
			prev_minor = Integer.parseInt(vmatch.group(2));
			prev_rev = Integer.parseInt(vmatch.group(3));
		} else {
			//shouldn't really happen, unless xml modification went haywire
		}
		
		boolean isoutdated = false;
		
		if(now_major > prev_major) {
			//Log.e("LAUNCHER","MAJOR NOW:" + now_major + " MAJOR PREV:" + prev_major);
			isoutdated = true;
		} else if (now_minor > prev_minor) {
			//Log.e("LAUNCHER","MINOR NOW:" + now_minor + " MINOR PREV:" + prev_minor);
			isoutdated = true;
		} else if (now_rev > prev_rev) {
			//Log.e("LAUNCHER","REV NOW:" + now_rev + " REV PREV:" + prev_rev);
			isoutdated = true;
		}
		
		if(isoutdated) {
			dowhatsnew = true;
			launcher_settings.setCurrentVersion(versionString);
			saveXML();
			//Log.e("LAUNCHER","DOING OUTATED, WAS " + launcher_settings.getCurrentVersion() + " NOW " + versionString);
		} else {
			//Log.e("LAUNCHER","NOT OUTDATED, WAS " + launcher_settings.getCurrentVersion() + " NOW " + versionString);
		}
		
		//if test mode, load test mode version
		//if(mode == LAUNCH_MODE.TEST) {
		if(ConfigurationLoader.isTestMode(this)) {
			int readver = this.getSharedPreferences("TEST_VERSION_DOWHATSNEW", Context.MODE_PRIVATE).getInt("TEST_VERSION", 0);
			int testVersion = 0;
			try {
				testVersion = this.getPackageManager().getApplicationInfo(launcher_source, PackageManager.GET_META_DATA).metaData.getInt("BLOWTORCH_TEST_VERSION");
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(testVersion != readver) {
				dowhatsnew = true;
				SharedPreferences.Editor edit = this.getSharedPreferences("TEST_VERSION_DOWHATSNEW", Context.MODE_PRIVATE).edit();
				edit.putInt("TEST_VERSION", testVersion);
				edit.commit();
			}
		}
		
		
		//getConnectionsFromDisk();
		
		Button newbutton = (Button)findViewById(R.id.new_connection);
		newbutton.setOnClickListener(new newClickedListener());
		
		Button helpbutton = (Button)findViewById(R.id.help_button);
		helpbutton.setOnClickListener(new helpClickedListener());
		
		Button donatebutton = (Button)findViewById(R.id.donate_button);
		donatebutton.setOnClickListener(new helpClickedListener());

		Log.e("LAUNCHER","STARTING SREVICE");
		String action = ConfigurationLoader.getConfigurationValue("serviceBindAction",Launcher.this);
		startService(new Intent(action,null,this, StellarService.class));
		buildList();
		if(!serviceBound) {
			//String action = ConfigurationLoader.getConfigurationValue("serviceBindAction",Launcher.this);
			bindService(new Intent(action,null,this, StellarService.class),connectionChecker,Context.BIND_AUTO_CREATE);
		}
		
	}
	
	private static void fixClassLoaderIssue()
	{
		ClassLoader myClassLoader = Launcher.class.getClassLoader();
		Thread.currentThread().setContextClassLoader(myClassLoader);
	}  
	
	public static boolean isOutDated(Context c) {
		
		//version number is major.minor.rev.[test]
		//method, load up version string, split on the "." character, convert parts to a number
		
		
		return false;
	}
	
	boolean checkedUpdate = false;
	
	public void onStart() {
		super.onStart();
		//if(noConnections) {
		//	Toast msg = Toast.makeText(this, "No connections specified, select NEW to create.", Toast.LENGTH_LONG);
		//	msg.show();
		//}
		if(ConfigurationLoader.isTestMode(this)) {
			if(!checkedUpdate) {
				checkedUpdate = true;
				BackgroundCheckUpdateThread t = new BackgroundCheckUpdateThread(updateHandler);
				t.start();
			}
		}
		
		if(dowhatsnew) {
			dowhatsnew = false;
			try {
				DoWhatsNew();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!serviceBound) {
			String action = ConfigurationLoader.getConfigurationValue("serviceBindAction",Launcher.this);
			bindService(new Intent(action,null,this, StellarService.class),connectionChecker,Context.BIND_AUTO_CREATE);
		}
	}
	
	boolean serviceBound = false;
	@Override
	public void onResume() {
		super.onResume();
		if(!serviceBound) {
			String action = ConfigurationLoader.getConfigurationValue("serviceBindAction",Launcher.this);
			bindService(new Intent(action,null,this, StellarService.class),connectionChecker,Context.BIND_AUTO_CREATE);
		}
		//buildList();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if(serviceConnected) {
			try {
				service.unregisterLauncherCallback(the_callback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		unbindService(connectionChecker);
		serviceBound = false;
		serviceConnected = false;
		//if(serviceConnected) {
		//	unbindService(connectionChecker);
		//}
	}
	
	
	public void onDestroy() {
		//saveConnectionsToDisk();
		saveXML();
		super.onDestroy();
	}
	
	private class helpClickedListener implements View.OnClickListener {

		public void onClick(View v) {
			Intent web_help = new Intent(Intent.ACTION_VIEW,Uri.parse("http://bt.happygoatstudios.com/"));
			startActivity(web_help);
		}
		
	}
	
	private class newClickedListener implements View.OnClickListener {
		public void onClick(View v) {
			//close the dialog for now
			//ConnectionPickerDialog.this.dismiss();
			NewConnectionDialog diag = new NewConnectionDialog(Launcher.this,Launcher.this);
			diag.show();
		}
	}
	
	private class listItemLongClicked implements ListView.OnItemLongClickListener {

		public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
				int arg2, long arg3) {
			//Log.e("LAUNCHER","List item long clicked!");
			MudConnection muc = apdapter.getItem(arg2);
			
			
			Message delmsg = connectionModifier.obtainMessage(MSG_DELETECONNECTION);
			delmsg.obj = muc;
			
			Message modmsg = connectionModifier.obtainMessage(MSG_MODIFYCONNECTION);
			modmsg.obj = muc;
			
			AlertDialog.Builder build = new AlertDialog.Builder(Launcher.this)
				.setMessage("Which operation to perform on: " + muc.getDisplayName());
			AlertDialog dialog = build.create();
			dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Edit", modmsg);
			dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Delete",delmsg);
			dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface arg0, int arg1) {
					arg0.dismiss();
				}
			});
			
			dialog.show();
			return true;
		}
		
	}
	boolean debug = true;
	private class listItemClicked implements ListView.OnItemClickListener {
		
		//@Override
		@TargetApi(11)
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			Rect rect = new Rect();
		    Window win = Launcher.this.getWindow();
		    win.getDecorView().getWindowVisibleDisplayFrame(rect);
		    int statusBarHeight = rect.top;
		    int contentViewTop = win.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		    int titleBarHeight = contentViewTop - statusBarHeight;
		    //Log.d("ID-ANDROID-CONTENT", "titleBarHeight = " + titleBarHeight );
		    
		    //if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
		    //	titleBarHeight += statusBarHeight;
		    //}
		    
		    SharedPreferences pref = Launcher.this.getSharedPreferences("STATUS_BAR_HEIGHT", 0);
			Editor e = pref.edit();
			e.putInt("STATUS_BAR_HEIGHT", statusBarHeight);
			e.putInt("TITLE_BAR_HEIGHT", titleBarHeight);
		    e.commit();
			
			MudConnection muc = apdapter.getItem(arg2);		
			
			Time the_time = new Time();
			the_time.set(System.currentTimeMillis());
			muc.setLastPlayed(the_time.format2445());
			
			saveXML();
			
			buildList();
			
			//if(debug) return;
			
			//Intent the_intent = new Intent(com.happygoatstudios.flamethrower_lib.window.MainWindow.class.getName());
	    	
	    	//the_intent.putExtra("DISPLAY",muc.getDisplayName());
	    	//the_intent.putExtra("HOST", muc.getHostName());
	    	//the_intent.putExtra("PORT", muc.getPortString());
	    	
	    	//write out the intent to the service so it can do some lookup work in advance of the connection, such as loading the settings wad
	    	//SharedPreferences prefs = Launcher.this.getSharedPreferences("SERVICE_INFO",0);
	    	//Editor edit = prefs.edit();
	    	//Log.e("WINDOW","SETTING " + muc.getDisplayName());
	    	
	    	
	    	//edit.putString("SETTINGS_PATH", muc.getDisplayName());
	    	//edit.commit();
	    	
	    	//check to see if the service is actually running
	    	
	    	//boolean found = isServiceRunning();
	    	
	    	//if(!found) {
    			//service is not running, reset the values in the shared prefs that the window uses to keep track of weather or not to finish init routines.
    			//kill all whitespace in the display name.
	    		launch = muc.copy();
	    		DoNewStartup();
	    	/*} else {
	    		//service exists, we should figure out the name of what it is playing.
	    		//Log.e("LAUNCHER","SERVICE IS RUNNING");
	    		launch = muc.copy();
	    		
	    		
	    		String action = ConfigurationLoader.getConfigurationValue("serviceBindAction",Launcher.this);
	    		bindService(new Intent(action),mConnection,0);
	    		
	    	}*/
	    	//}
	    	
		}

		
	}
	private MudConnection launch;
	
//	private ServiceConnection mConnection = new ServiceConnection() {
//
//		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
//			
//			if(arg1 == null) {
//				return;
//			}
//			
//			service = IConnectionBinder.Stub.asInterface(arg1); //turn the binder into something useful
//			
//			String test = "";
//			String against = launch.getHostName() +":"+ launch.getPortString();
//			try {
//				test = service.getConnectedTo();
//			} catch (RemoteException e) {
//				throw new RuntimeException(e);
//			}
//			
//			if(!test.equals(against)) {
//				//does not equal, show the warning.
//				AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
//				builder.setMessage("Service already connected to " + test + "\nDisconnect and launch " + launch.getDisplayName() + "?");
//				builder.setTitle("Currently Connected");
//				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//					
//					public void onClick(DialogInterface dialog, int which) {
//						Launcher.this.unbindService(connectionChecker);
//						stopService(new Intent(es.reinosdeleyenda.flamethrower_lib.service.IConnectionBinder.class.getName()));
//						DoNewStartup();
//					}
//				});
//				
//				builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
//					
//					public void onClick(DialogInterface dialog, int which) {
//						Launcher.this.unbindService(connectionChecker);
//						dialog.dismiss();
//					}
//				});
//				AlertDialog connected = builder.create();
//				connected.show();
//			} else {
//				//are equal, proceed with normal startup.
//				Launcher.this.unbindService(connectionChecker);
//				DoFinalStartup();
//			}
//			
//		}

	

//		public void onServiceDisconnected(ComponentName arg0) {
//			
//		}
//		
//	};
	public boolean serviceConnected = false;
	private ServiceConnection connectionChecker = new ServiceConnection() {

		//private boolean connected = false;
		
		public void onServiceConnected(ComponentName arg0, IBinder arg1) {
			Launcher.this.serviceConnected = true;
			Log.e("LAUNCHER","SERVICE CONNECTED");
			service = IConnectionBinder.Stub.asInterface(arg1);
			try {
				service.registerLauncherCallback(the_callback);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			serviceBound = true;
			serviceConnected = true;
			//try {
				//connectedList = (List<String>)tmp.getConnections();
			//} catch (RemoteException e) {
				// TODO Auto-generated catch block
			//	e.printStackTrace();
			//}
			//try {
				//Launcher.this.unbindService(this);
			//} catch (IllegalArgumentException e) {
				
			//}
			buildList();
			
//			if(connectedList != null) {
//				for(int i=0;i<apdapter.getCount();i++) {
//					apdapter.getItem(i).setConnected(connectedList.contains(apdapter.getItem(i).getDisplayName()));
//				}
//			}
//			
//			apdapter.notifyDataSetInvalidated();
		}

		public void onServiceDisconnected(ComponentName name) {
			Launcher.this.serviceConnected = false;
			Log.e("LAUNCHER","SERVICE DISCONNECTED");
		}
		
		//public boolean isConnected() {
		//	return connected;
		//}
		
		
	};
	
	List<String> connectedList = null;
	
	
	public final int MSG_DELETECONNECTION = 101;
	public final int MSG_MODIFYCONNECTION = 102;
	public Handler connectionModifier = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_DELETECONNECTION:
				MudConnection todelete = (MudConnection)msg.obj;
				launcher_settings.getList().remove(todelete.getDisplayName());
				buildList();
				//MudConnection todelete = (MudConnection)msg.obj;
				//apdapter.remove(todelete);
				//apdapter.notifyDataSetChanged();
				break;
			case MSG_MODIFYCONNECTION:
				MudConnection tomodify = (MudConnection)msg.obj;
				NewConnectionDialog diag = new NewConnectionDialog(Launcher.this,Launcher.this,tomodify);
				diag.show();
				break;
			default:
				break;
			}
		}
	};
	
	public void ready(MudConnection newData) {
		//promote this one to the head of the class.
		Time t = new Time();
		t.set(System.currentTimeMillis());
		newData.setLastPlayed(t.format2445());
		launcher_settings.getList().put(newData.getDisplayName(), newData);
		buildList();
		saveXML();
	}

    /*public void ready(String displayname,String host,String port) {
    	
    	
		MudConnection muc = new MudConnection();
		muc.setDisplayName(displayname);
		muc.setHostName(host);
		muc.setPortString(port);
		
		launcher_settings.getList().put(muc.getDisplayName(), muc.copy());
		buildList();


    	
    }*/
    
    public void modify(MudConnection old, MudConnection newData) {
    	launcher_settings.getList().remove(old.getDisplayName());
    	launcher_settings.getList().put(newData.getDisplayName(), newData);
    	buildList();
    	saveXML();
    }
    
	/*public void modify(String displayname, String host, String port,MudConnection old) {

		MudConnection muc = new MudConnection();
		muc.setDisplayName(displayname);
		muc.setHostName(host);
		muc.setPortString(port);
		
		apdapter.remove(old);
		
		apdapter.add(muc);
		apdapter.notifyDataSetChanged();
	}*/
    
	private void getConnectionsFromDisk() {
		//This is here for posterity. It will only be used to fallback.
		
		SharedPreferences pref = this.getSharedPreferences(PREFS_NAME, 0);
		
		String thestring = pref.getString("STRINGS", "");
		if(thestring == null || thestring == "") { return; }
		
		Pattern connection = Pattern.compile("([^\\|]+)");
		Pattern breakout = Pattern.compile("(.+):(.+):(.+)");
		
		Matcher c_m = connection.matcher(thestring);
		
		while(c_m.find()) {
			String operate = c_m.group(1);
			Matcher o_m = breakout.matcher(operate);
			while(o_m.find()) {
				String displayname = o_m.group(1);
				String hostname = o_m.group(2);
				String portstring = o_m.group(3);
				
				MudConnection muc = new MudConnection();
				muc.setDisplayName(displayname);
				muc.setHostName(hostname);
				muc.setPortString(portstring);
				
				apdapter.add(muc);
			}
		}
		
	}
	
	private void DoImportMenu(boolean external) {
		
		File tmp = Environment.getExternalStorageDirectory();
		String dir = SDCardUtils.getSDCardRoot(this, external);
		File btermdir = null;
		if(external) {
			btermdir = new File(tmp, dir + "/launcher/");
		} else {
			btermdir = new File(dir + "/launcher/");
		}
		
		String sdstate = Environment.getExternalStorageState();
		HashMap<String,String> xmlfiles = new HashMap<String,String>();
		if(Environment.MEDIA_MOUNTED.equals(sdstate) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(sdstate)) {
			btermdir.mkdirs();

			String[] list = btermdir.list(xml_only);
			if(list == null || list.length == 0) {
				Toast t = Toast.makeText(this, "No XML files in /BlowTorch/launcher/", Toast.LENGTH_LONG);
				t.show();
				return;
			}

			for(File xml : btermdir.listFiles(xml_only)) {
				xmlfiles.put(xml.getName(), xml.getPath());
			}
			
			final String[] entries = new String[xmlfiles.keySet().size()];
			String[] names = new String[xmlfiles.keySet().size()];
			
			if(xmlfiles.size() == 0) {
				Toast t = Toast.makeText(this, "No XML files in /BlowTorch/launcher/", Toast.LENGTH_LONG);
				t.show();
				return;
			}
			
			int i=0;
			for(String name : xmlfiles.keySet()) {
				names[i] = name;
				entries[i] = xmlfiles.get(name);
				i++;
			}
			
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select List");
			builder.setSingleChoiceItems(names, -1,new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface dialog, int which) {
					actionHandler.sendMessage(actionHandler.obtainMessage(MESSAGE_IMPORT, entries[which]));
					dialog.dismiss();
				}
			});
			
			//builder.setI
			
			AlertDialog dialog = builder.create();
			dialog.show();
			
		} else {
			Toast t = Toast.makeText(this, "SD card not available.", Toast.LENGTH_LONG);
			t.show();
		}
		
		
	}
	
	FilenameFilter xml_only = new FilenameFilter() {

		public boolean accept(File arg0, String arg1) {
			//return arg1.endsWith(".xml");
			xmlimatcher.reset(arg1);
			if(xmlimatcher.matches()) {
				return true;
			} else {
				return false;
			}
		}
		
	};
	
	private boolean isServiceRunning() {
		ActivityManager activityManager = (ActivityManager)Launcher.this.getSystemService(Context.ACTIVITY_SERVICE);
    	List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
    	boolean found = false;
    	String serviceName = "es.reinosdeleyenda.flamethrower_lib" + ConfigurationLoader.getConfigurationValue("serviceProcessName",this);
    	
    	for(RunningServiceInfo service : services) {
    		//Log.e("LAUNCHER","FOUND:" + service.service.getClassName());
    		//service.service.
    		if(StellarService.class.getName().equals(service.service.getClassName())) {
    			//service is running, don't do anything.
    			//Log.e(":Launcher","Service lives in: " + service.process);
    			if(service.process.equals(serviceName)) found = true;
    			/*if(mode == LAUNCH_MODE.FREE) {
    				
    				if(service.process.equals("com.happygoatstudios.flamethrower_lib:stellar_free")) found = true;
    			} else if(mode == LAUNCH_MODE.TEST) {
    				if(service.process.equals("com.happygoatstudios.flamethrower_lib:stellar_test")) found = true;
    			}*/
    			
    		} else {

    			
    		}
    	}
		return found;
	}
	
	private void DoExport(String filename, boolean external) {

		String dir = null;
		try {
			Context c = this.createPackageContext(this.getPackageName(), Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
			dir = (external == true) ? "/BlowTorch" : c.getExternalFilesDir(null).getAbsolutePath();
		} catch(NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		//String dir = "/BlowTorch";
		String launcher = "/launcher";
		String path = dir + launcher + filename;
		
		try {
			//tmp = BaardTERMService.this.openFileOutput(path, Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);
			//BaardTERMService.this.openF
			String message = null;
			File root = Environment.getExternalStorageDirectory();
			String state = Environment.getExternalStorageState();
			if(Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				boolean added = false;
				String updated = path;
				Pattern xmlend = Pattern.compile("^.+\\.[Xx][Mm][Ll]$");
				Matcher xmlmatch = xmlend.matcher(updated);
				String updatedname = filename;
				if(!xmlmatch.matches()) {
					//added = true;
					//updated = path + ".xml";
					updatedname = filename + ".xml";
				}
				
				File blowtorchdir = null;
				if(external == true) {
					blowtorchdir =  new File(root, dir);
				} else {
					blowtorchdir = new File(dir);
				}
				blowtorchdir.mkdirs();
				
				
				File launcherdir = new File(blowtorchdir,launcher);
				launcherdir.mkdirs();
				
				File file = new File(launcherdir,updatedname);
				
				file.createNewFile();
				
				FileWriter writer = new FileWriter(file);
				BufferedWriter tmp = new BufferedWriter(writer);
				tmp.write(LauncherSettings.writeXml(launcher_settings));
				tmp.close();
				
				message = "Saved: " + file.getPath();
				if(external == false) {
					message += "This file will be removed when the application is uninstalled.";
				}
				
				//Toast msg = Toast.makeText(this,message,Toast.LENGTH_LONG);
				//Toast msg = Toast.makeText(StellarService.this.getApplicationContext(), message, Toast.LENGTH_SHORT);
				//msg.show();
			} else {
				//Log.e("SERVICE","COULD NOT WRITE SETTINGS FILE!");
				//Toast msg = Toast.makeText(StellarService.this.getApplicationContext(), "SD Card not available. File not written.", Toast.LENGTH_SHORT);
				//msg.show();
				message = "SD Card not available. File not written.";
				//msg.show();
			}
			Snackbar bar = Snackbar.make(findViewById(R.id.launcher_window_content), message,
					Snackbar.LENGTH_INDEFINITE)
					.setAction(android.R.string.ok,new View.OnClickListener() {
						@Override
						public void onClick(View view) {

						}});

			//View snackbarView = bar.getView();
			//TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
			//textView.setMaxLines(5);  // show multiple line
			bar.show();
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
	}
	
	
	private void DoRecovery(String targetPackage,boolean external) throws NameNotFoundException {


		
		Context c = this.createPackageContext(targetPackage, Context.CONTEXT_INCLUDE_CODE|Context.CONTEXT_IGNORE_SECURITY);
		String dir = (external == true) ? "/BlowTorch" : c.getExternalFilesDir(null).getAbsolutePath();
		String backupDir = "/recovered/";

		String targetInstallation = c.getApplicationInfo().dataDir + "/files";
		String message = null;
		try {
			File root = Environment.getExternalStorageDirectory();
			String state = Environment.getExternalStorageState();
			if(Environment.MEDIA_MOUNTED.equals(state) && !Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
				//make sure destination directory exists
				File btdir = null;
				if(external) {
					btdir = new File(root,dir);
				} else {
					btdir = new File(dir,"/");
				}
				btdir.mkdir();
				
				File backupdir = new File(btdir,backupDir);
				backupdir.mkdir();
				
				//get all the files in the target directory.
				File harvestDir = new File(targetInstallation);
				
				/*InputStream inp = c.openFileInput("blowtorch_launcher_list.xml");
				byte[] buff = new byte[1024];
				int len2 = 0;
				while((len2 = inp.read(buff)) > 0) {
					Log.e("FOO",new String(buff));
				}*/
				//iterate through copying to backup directory.
				String[] names = harvestDir.list();
				for(String name : names) {
					
					File oldFile = new File(harvestDir,name);
					File newFile = new File(backupdir,name);
					
					InputStream in = new FileInputStream(oldFile);
					OutputStream out = new FileOutputStream(newFile);
					//OutputStream out = new 
					byte[] buf = new byte[1024];
					int len;
					while((len = in.read(buf)) > 0) {
						out.write(buf,0,len);
					}
					
					in.close();
					out.close();
					
				}

				message =  "Settings copied to: " + backupdir.getAbsolutePath() + "/";
				if(external == false) {
					message += "\nThis folder will be removed when the application is uninstalled.";
				}
				//Toast t = Toast.makeText(this, "Settings copied to: " + backupdir.getAbsolutePath() + "/", Toast.LENGTH_LONG);
				//t.show();
			} else {
				message = "SD Card Unavailabe. Cannot recover settings.";
				//Toast t = Toast.makeText(this, "SD Card Unavailabe. Cannot recover settings.", Toast.LENGTH_LONG);
				//t.show();
			}
			Snackbar bar = Snackbar.make(findViewById(R.id.launcher_window_content), message,
					Snackbar.LENGTH_INDEFINITE)
					.setAction(android.R.string.ok,new View.OnClickListener() {
						@Override
						public void onClick(View view) {

						}});

			View snackbarView = bar.getView();
			TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
			textView.setMaxLines(3);  // show multiple line
			bar.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void DoWhatsNew() throws NameNotFoundException { 
		
		//get the version information.
		PackageManager m = this.getPackageManager();
		String versionString = null;
		try {
			versionString = m.getPackageInfo(launcher_source, PackageManager.GET_CONFIGURATIONS).versionName;
		} catch (NameNotFoundException e) {
			//can't execute on our package aye?
			throw new RuntimeException(e);
		}
		
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		//if(mode == LAUNCH_MODE.TEST) {
		if(ConfigurationLoader.isTestMode(this)) {
			int testVersion = this.getPackageManager().getApplicationInfo(launcher_source, PackageManager.GET_META_DATA).metaData.getInt("BLOWTORCH_TEST_VERSION");
			builder.setTitle("Version " + versionString + "t"+testVersion+" details!");
			
			final SpannableString s = new SpannableString(Launcher.this.getResources().getString(R.string.whatisnew_test));
		    Linkify.addLinks(s, Linkify.ALL);
	
			builder.setMessage(s);
		} else {
			builder.setTitle("Version " + versionString + " details!");
			
			final SpannableString s = new SpannableString(Launcher.this.getResources().getString(R.string.whatisnew));
		    Linkify.addLinks(s, Linkify.ALL);
	
			builder.setMessage(s);
		}
		builder.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		
		AlertDialog diag = builder.create();
		
		//TextView message = (TextView) diag.findViewById(android.R.id.message);
		
		diag.show();
		
		((TextView)diag.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());

		
	}
	
	private void DoNewStartup() {
		Pattern invalidchars = Pattern.compile("\\W"); 
		Matcher replacebadchars = invalidchars.matcher(launch.getDisplayName());
		String prefsname = replacebadchars.replaceAll("") + ".PREFS";
		//prefsname = prefsname.replaceAll("/", "");
		
		SharedPreferences sprefs = Launcher.this.getSharedPreferences(prefsname,0);
		//servicestarted = prefs.getBoolean("CONNECTED", false);
		//finishStart = prefs.getBoolean("FINISHSTART", true);
		SharedPreferences.Editor editor = sprefs.edit();
		editor.putBoolean("CONNECTED", false);
		editor.putBoolean("FINISHSTART", true);
		editor.commit();
		//Log.e("LAUNCHER","SERVICE NOT STARTED, AM RESETTING THE INITIALIZER BOOLS IN " + prefsname);
		
		//Launcher.this.startActivity(the_intent);
		//SharedPreferences sprefs = Launcher.this.getSharedPreferences(prefsname,0);
		//SharedPreferences.Editor editor = sprefs.edit();
		//editor.putBoolean("CONNECTED", false);
		//editor.putBoolean("FINISHSTART", true);
		editor.commit();
		
		
		//launch = muc;
		DoFinalStartup();
	}
	
	private void DoFinalStartup() {
		Intent the_intent = null;
		/*if(mode == LAUNCH_MODE.TEST) {
			the_intent = new Intent("com.happygoatstudios.flamethrower_lib.window.MainWindow.TEST_MODE");
		} else {
			//Log.e("BlowTorch","LAUNCHING NORMAL MODE!");
			the_intent = new Intent("com.happygoatstudios.flamethrower_lib.window.MainWindow.NORMAL_MODE");
		}*/
		
		String windowAction = ConfigurationLoader.getConfigurationValue("windowAction",this);
		the_intent = new Intent(windowAction);
    	the_intent.putExtra("DISPLAY",launch.getDisplayName());
    	the_intent.putExtra("HOST", launch.getHostName());
    	the_intent.putExtra("PORT", launch.getPortString());
    	
    	//write out the intent to the service so it can do some lookup work in advance of the connection, such as loading the settings wad
    	//SharedPreferences prefs = Launcher.this.getSharedPreferences("SERVICE_INFO",0);
    	//Editor edit = prefs.edit();
    	//Log.e("WINDOW","SETTING " + muc.getDisplayName());
    	
    	
    	//edit.putString("SETTINGS_PATH", launch.getDisplayName());
    	//edit.commit();
		//Pattern invalidchars = Pattern.compile("\\W"); 
		//Matcher replacebadchars = invalidchars.matcher(launch.getDisplayName());
		//String prefsname = replacebadchars.replaceAll("") + ".PREFS";
		///prefsname = prefsname.replaceAll("/", "");
		
		
		//Log.e("LAUNCHER","SERVICE NOT STARTED, AM RESETTING THE INITIALIZER BOOLS IN " + prefsname);
		
    	
    	SharedPreferences prefs = Launcher.this.getSharedPreferences("SERVICE_INFO",0);
    	Editor edit = prefs.edit();
    	
    	
    	edit.putString("SETTINGS_PATH", launch.getDisplayName());
    	edit.commit();
    	
    	//this.unbindService(connectionChecker);
    	
		Launcher.this.startActivity(the_intent);
	}
	
	private ConnectionComparator ccmp = new ConnectionComparator();
	
	private void buildList() {
		apdapter.clear();
		
		
		for(MudConnection m : launcher_settings.getList().values()) {
			
			
			apdapter.add(m);
		}
		
		apdapter.sort(ccmp);
		
		if(serviceBound) {
			try {
				connectedList = (List<String>)service.getConnections();
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			if(connectedList != null) {
				for(int i=0;i<apdapter.getCount();i++) {
					apdapter.getItem(i).setConnected(connectedList.contains(apdapter.getItem(i).getDisplayName()));
				}	
			}
		}
		
		//String action = ConfigurationLoader.getConfigurationValue("serviceBindAction",Launcher.this);
		//if(!serviceConnected) {
			//this.startService(new Intent(action));
			//fgds
		//} else {
			
		//}
		apdapter.notifyDataSetChanged();
		//this.bindService(service, conn, flags)
	}
	
	private void saveXML() {
		try {
			FileOutputStream fos = this.openFileOutput("blowtorch_launcher_list.xml",Context.MODE_PRIVATE);
			fos.write(LauncherSettings.writeXml(launcher_settings).getBytes("UTF-8"));
			fos.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private class ConnectionComparator implements Comparator<MudConnection> {

		public int compare(MudConnection a, MudConnection b) {
			//pos it above, negative if below
			Time at = new Time();
			Time bt = new Time();
			
			//check if either have haver been played.
			if(a.getLastPlayed().equals("never")) {
				return 1;
			} else if(b.getLastPlayed().equals("never")) {
				return -1;
			} else if(b.getLastPlayed().equals("never") && a.getLastPlayed().equals("never")){
				return 0; //they are both never, so they are equal.
			}
			
			try{
				
				at.parse(a.getLastPlayed());
				bt.parse(b.getLastPlayed());
			} catch (TimeFormatException e) {
				return 0;
			}
			return Time.compare(bt, at);
		}
		
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		
		menu.add(0,99,0,"What's New");
		menu.add(0,100,0,"Import List");
		menu.add(0,105,0,"Export List");
		if(ConfigurationLoader.isTestMode(this)) menu.add(0,106,0,"User Name");
		menu.add(0,107,0,"Recover Settings");
		menu.add(0, 108, 0,"SDCard Permissions");
		menu.add(0, 109, 0,"App Settings");
		
		return true;
		
	}

	private void AskExportFileName(final boolean external) {

		LayoutInflater factory = LayoutInflater.from(this);
		View textEntryView = factory.inflate(R.layout.dialog_text_entry, null);
		entry = (EditText) textEntryView.findViewById(R.id.launcher_export);

		AlertDialog exporter = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("Export List")
				.setView(textEntryView)
				.setPositiveButton("Done", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//boolean state = SDCardUtils.hasPermissions(Launcher.this,findViewById(R.id.launcher_window_content), RP_INFO);
						//if(external == true) {
							DoExport(entry.getText().toString(),external);
						//}
					}
				})

				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						dialog.dismiss();
					}
				})
				.create();

		exporter.show();
	}
	
	private EditText entry = null;
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case 99:
			//dowhatsnew
			try {
				DoWhatsNew();
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 100:
			//start import
			//DoImportMenu();
			if(SDCardUtils.hasPermissions(this,findViewById(R.id.launcher_window_content), RP_IMPORT)) {
				//DoImportMenu();
				showImportMessage(true);
			}
			break;
		case 105:
			//start export
			if(SDCardUtils.hasPermissions(this,findViewById(R.id.launcher_window_content), RP_EXPORT)) {
				//actionHandler.sendMessage(actionHandler.obtainMessage(MESSAGE_DORECOVERY, this.getPackageName()));
				AskExportFileName(true);
			}//false is handled by the activity interface implementation

			break;
		case 106:

			break;
		case 107:
			//data recovery.
			//figure out if the release package is installed.
			/*boolean retailInstalled = false;
			try {
				Context c = this.createPackageContext("com.happygoatstudios.flamethrower_lib", Context.CONTEXT_IGNORE_SECURITY|Context.CONTEXT_INCLUDE_CODE);
				retailInstalled = true;
			} catch (NameNotFoundException e) {
				retailInstalled = false;
			}
			
			final String[] names;
			final String[] values;
			if(retailInstalled) {
				names = new String[] {"BlowTorch (Release)", "BlowTorch (Test)" };
				values = new String[] {"com.happygoatstudios.flamethrower_lib","es.reinosdeleyenda.flamethrowertest"};
			} else {
				names = new String[] {"BlowTorch (Test)" };
				values = new String[] {"es.reinosdeleyenda.flamethrowertest"};
			}
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setSingleChoiceItems(names, 0, new DialogInterface.OnClickListener() {
				
				
				public void onClick(DialogInterface dialog, int which) {
					
					//switch(which) {
						actionHandler.sendMessage(actionHandler.obtainMessage(MESSAGE_DORECOVERY,values[which]));
					//}
					dialog.dismiss();
				}
			});
			
			AlertDialog dialog = builder.create();
			dialog.show();*/
			if(SDCardUtils.hasPermissions(this,findViewById(R.id.launcher_window_content), RP_SALVAGE)) {
				//actionHandler.sendMessage(actionHandler.obtainMessage(MESSAGE_DORECOVERY, this.getPackageName()));
				try {
					DoRecovery(this.getPackageName(), true);
				} catch(Exception e) {
					throw new RuntimeException(e) ;
				}
			}//false is handled by the activity interface implementation
			
			break;
		case 108:
			// Here, thisActivity is the current activity
			boolean state = SDCardUtils.hasPermissions(this,findViewById(R.id.launcher_window_content), RP_INFO);
			String message = "SDCard permissions are granted. If this is incorrect, click this message to manage settings.";


			if(state == true) {
				showPermissionsMessage(true);
			} // false is handled by the request permissions dialog (i think).
			break;
		case 109:
			Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse("package:" + Launcher.this.getPackageName()));
			Launcher.this.startActivity(intent);
			break;

		default:
			break;
		}
		
		return true;
	}
	
	
    
	private class ConnectionAdapter extends ArrayAdapter<MudConnection> {
		private ArrayList<MudConnection> items;
		private int textcolor;
		public ConnectionAdapter(Context context, int txtviewresid, ArrayList<MudConnection> objects) {
			super(context, txtviewresid, objects);
			
			this.items = objects;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			if(v == null) {
				LayoutInflater li = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = li.inflate(R.layout.connection_row, null);
				//TextView title = (TextView)v.findViewById(R.id.displayname);
				//textcolor = title.getText
			}
			
			MudConnection m = items.get(position);
			if(m != null) {
				TextView title = (TextView)v.findViewById(R.id.displayname);
				TextView host = (TextView)v.findViewById(R.id.hoststring);
				//TextView port = (TextView)v.findViewById(R.id.port);
				if(title != null) {
					title.setText(" " + m.getDisplayName());
				}
				if(host != null) {
					host.setText("\t"  + m.getHostName() + ":" + m.getPortString());
				}
				
				if(m.isConnected()) {
					title.setTextColor(0xFF00FF00);
				} else {
					title.setTextColor(0xAA222222);
				}
				//if(port != null) {
				//	port.setText(" Port: " + m.getPortString());
				//}
			}
			return v;
			
			
		}
		
		
	}
	
	private final int MESSAGE_STARTUPDATE = 10098;
	private final int MESSAGE_STARTDOWNLOAD = 10099;
	private final int MESSAGE_CANCELDOWNLOAD = 10100;
	private final int MESSAGE_FINISHUPDATE = 10101;
	private final int MESSAGE_DOWNLOADEDBYTES = 10102;
	private final int MESSAGE_UPTODATE = 10103;
	private final int MESSAGE_NOSDCARD = 10104;
	private final int MESSAGE_BYTESINCOMING = 10105;
	private final int MESSAGE_NEEDSUPDATE = 10106;
	ProgressDialog updateDialog = null;
	UpdateThread update = null;
	Handler updateHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MESSAGE_NEEDSUPDATE:
				AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
				builder.setTitle("Update Available");
				builder.setMessage("An update is available for this package, would you like to update now?");
				builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						UpdateThread t = new UpdateThread(updateHandler);
						t.start();
						dialog.dismiss();
					}
				});
				builder.setNegativeButton("No",new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
					
				});
				AlertDialog d = builder.create();
				d.show();
				break;
			case MESSAGE_BYTESINCOMING:
				updateDialog.setMessage("Downloading "+(Integer)msg.obj+"bytes.");
				updateDialog.setMax((Integer)msg.obj);
				break;
			case MESSAGE_DOWNLOADEDBYTES:
				updateDialog.incrementProgressBy(msg.arg1);
				break;
			case MESSAGE_STARTUPDATE:
				updateDialog = ProgressDialog.show(Launcher.this,"","Checking update status.",true,true,new DialogInterface.OnCancelListener() {
					
					@Override
					public void onCancel(DialogInterface dialog) {
						return;
					}
				});
				break;
			case MESSAGE_NOSDCARD:
				Toast nodsd = Toast.makeText(Launcher.this, "External storage is unavailable to write to, cannot download update.", Toast.LENGTH_SHORT);
				nodsd.show();
				break;
			case MESSAGE_UPTODATE:
				//Integer newVersion = Integer.parseInt(buf.toString());
				//Log.e("BlowTorch","Web update version: " + newVersion);
				updateDialog.dismiss();
				
				ApplicationInfo testLauncher = null;
				try {
					testLauncher = Launcher.this.getPackageManager().getApplicationInfo(launcher_source, PackageManager.GET_META_DATA);
				} catch (NameNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				int testversionName = testLauncher.metaData.getInt("BLOWTORCH_TEST_VERSION");
				
				Toast t = Toast.makeText(Launcher.this, "BlowTorch Test Version "+testversionName+" is up to date.", Toast.LENGTH_SHORT);
				t.show();
				break;
			case MESSAGE_STARTDOWNLOAD:
				
				updateDialog.dismiss();
				updateDialog = null;
				
				updateDialog = new ProgressDialog(Launcher.this);
				updateDialog.setCancelable(true);
				updateDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				updateDialog.setMessage("Starting download.");
				updateDialog.setCancelMessage(this.obtainMessage(MESSAGE_CANCELDOWNLOAD));
				//updateDialog.setMax(size);
				//updateDialog.setProgress(0);
				updateDialog.show();
				
				
				
//				synchronized(this) {
//				try {
//					
//					this.wait(50);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				}
				/*URL updateSize;
				try {
					updateSize = new URL("http://bt.happygoatstudios.com/test/size");
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				BufferedReader in;
				try {
					in = new BufferedReader(new InputStreamReader(updateSize.openStream()));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				String tmp = "";
				StringBuffer buf = new StringBuffer();
				try {
					while((tmp = in.readLine())!=null) {
						buf.append(tmp);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				Integer size = Integer.parseInt(buf.toString());
				Log.e("BlowTorch","Update size is: " + size + " bytes.");*/
				
				
				
				
				//update = new UpdateThread(this);
				//update.run();
				
				
				break;
			case MESSAGE_CANCELDOWNLOAD:
				update.doCancel();
				if(updateDialog != null) {
					updateDialog.dismiss();
				}
				updateDialog = null;
				String delyou = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BlowTorch/launcher/TestPackage.apk";
				//proceed with download.
				File delme = new File(delyou);
				if(delme.exists()) delme.delete();
				break;
			case MESSAGE_FINISHUPDATE:
				updateDialog.dismiss();
				updateDialog = null;
				update = null;
				String updatepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BlowTorch/launcher/TestPackage.apk";
				File file = new File(updatepath);
				if(!file.exists()) {
					//Log.e("BlowTorch","Test application update does not exist.");
					return; //file doesn't exist
				}
				
				Intent i = new Intent();
				i.setAction(Intent.ACTION_VIEW);
				Uri data = Uri.parse("file://" + updatepath);
				i.setDataAndType(data, "application/vnd.android.package-archive");
				startActivity(i);
				Launcher.this.finish();
				break;
			}
		}
	};

	private class UpdateThread extends Thread {
		
		private boolean cancelled = false;
		private Handler reportTo = null;
		
		public UpdateThread(Handler useMe) {
			reportTo = useMe;
		}
		
		public void doCancel() {
			cancelled = true;
		}
		
		public void run() {
			//check if we need to download.
			reportTo.sendEmptyMessage(MESSAGE_STARTUPDATE);
			
			URL url2 = null;
			try {
				url2 = new URL("http://bt.happygoatstudios.com/test/version");
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				URLConnection c = url2.openConnection();
				
				c.setUseCaches(false);
				BufferedReader in = new BufferedReader(new InputStreamReader(c.getInputStream()));
				StringBuffer buf = new StringBuffer();
				String tmp;
				while((tmp = in.readLine()) != null) {
					buf.append(tmp);
				}
				try {
					String data = buf.toString();
					Integer newVersion = Integer.parseInt(data);
					//Log.e("BlowTorch","Web update version: " + newVersion);
					ApplicationInfo testLauncher = Launcher.this.getPackageManager().getApplicationInfo(launcher_source, PackageManager.GET_META_DATA);
					int testversionName = testLauncher.metaData.getInt("BLOWTORCH_TEST_VERSION");
					int testversion = newVersion;
					PackageManager pm = Launcher.this.getPackageManager();
					testversion = pm.getPackageInfo(testLauncher.packageName, PackageManager.GET_CONFIGURATIONS).versionCode;
					if (newVersion > testversion) {
						//needsupdate = true;
					} else {
						//Toast t = Toast.makeText(Launcher.this, "BlowTorch Test Version "+testversionName+" is up to date.", Toast.LENGTH_SHORT);
						//t.show();
						//updateDialog.dismiss();
						//updateDialog = null;
						reportTo.sendEmptyMessage(MESSAGE_UPTODATE);
						
						return;
					}
				} catch(NumberFormatException e) {
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String state = Environment.getExternalStorageState();
			if(!state.equals(Environment.MEDIA_MOUNTED)) {
				//no sd card
				reportTo.sendEmptyMessage(MESSAGE_NOSDCARD);
				return;
			} else {
				
				//updateDialog = ProgressDialog.show(Launcher.this, "", "Downloading update.",true,true,new DialogInterface.OnCancelListener() {
					
				//	public void onCancel(DialogInterface dialog) {
						
				//	}
				//});
				
				//updateHandler.sendEmptyMessageDelayed(MESSAGE_STARTDOWNLOAD,1000);
				
			}
			
			reportTo.sendEmptyMessage(MESSAGE_STARTDOWNLOAD);

			String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BlowTorch/launcher/TestPackage.apk";
			
			File deleter = new File(filename);
			if(deleter.exists()) deleter.delete();
			
			URL url = null;
			try {
				url = new URL("http://bt.happygoatstudios.com/test/TestPackage.apk");
				
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				
				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();
				
				//discover byte size and report it to the progress dialog.
				Message m = reportTo.obtainMessage(MESSAGE_BYTESINCOMING,c.getContentLength());
				reportTo.sendMessage(m);
				String updatepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BlowTorch/launcher/TestPackage.apk";
				String btdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BlowTorch/";
				//String launcherdir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/BlowTorch/launcher/";
				File btdirF = new File(btdir);
				File launcherdirF = new File(btdirF,"/launcher/");
				if(!btdirF.exists()) {
					btdirF.mkdirs();
				}
				
				if(!launcherdirF.exists()) {
					launcherdirF.mkdirs();
				}
				
				File file = new File(updatepath);
				
				if(!file.exists()) {
					boolean test = file.createNewFile();
					if(test) {
						long foo = System.currentTimeMillis();
					}
				}
				
				FileOutputStream fos = new FileOutputStream(file);
				
				InputStream is = c.getInputStream();
				//is.
				
				byte[] buffer = new byte[8192];
				int len = 0;
				while((len = is.read(buffer,0,8192)) != -1 && !cancelled) {
					fos.write(buffer,0,len);
					reportTo.sendMessage(reportTo.obtainMessage(MESSAGE_DOWNLOADEDBYTES,len,0));
				}
				
				fos.close();
				is.close();
				
				reportTo.sendEmptyMessage(MESSAGE_FINISHUPDATE);
				
				return;
				
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
				reportTo.sendEmptyMessage(MESSAGE_CANCELDOWNLOAD);
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				reportTo.sendEmptyMessage(MESSAGE_CANCELDOWNLOAD);
				throw new RuntimeException(e);
			}
		}
	}
	
	public class BackgroundCheckUpdateThread extends Thread {
		
		Handler reportTo = null;
		public BackgroundCheckUpdateThread(Handler h) {
			reportTo = h;
		}
		
		public void run() {
			//check if we need to download.
			//reportTo.sendEmptyMessage(MESSAGE_STARTUPDATE);
			
			URL url2 = null;
			try {
				url2 = new URL("http://bt.happygoatstudios.com/test/version");
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(url2.openStream()));
				StringBuffer buf = new StringBuffer();
				String tmp;
				while((tmp = in.readLine()) != null) {
					buf.append(tmp);
				}
				try {
					Integer newVersion = Integer.parseInt(buf.toString());
					//Log.e("BlowTorch","Web update version: " + newVersion);
					ApplicationInfo testLauncher = Launcher.this.getPackageManager().getApplicationInfo(launcher_source, PackageManager.GET_META_DATA);
					int testversionName = testLauncher.metaData.getInt("BLOWTORCH_TEST_VERSION");
					int testversion = newVersion;
					PackageManager pm = Launcher.this.getPackageManager();
					testversion = pm.getPackageInfo(testLauncher.packageName, PackageManager.GET_CONFIGURATIONS).versionCode;
					if(newVersion > testversion) {
						//needsupdate = true;
						reportTo.sendEmptyMessage(MESSAGE_NEEDSUPDATE);
					} else {
						//Toast t = Toast.makeText(Launcher.this, "BlowTorch Test Version "+testversionName+" is up to date.", Toast.LENGTH_SHORT);
						//t.show();
						//updateDialog.dismiss();
						//updateDialog = null;
						//reportTo.sendEmptyMessage(MESSAGE_UPTODATE);
						
						return;
					}
				} catch(NumberFormatException e) {
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	ILauncherCallback the_callback = new ILauncherCallback.Stub() {

		@Override
		public void connectionDisconnected() throws RemoteException {
			Launcher.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Launcher.this.buildList();
				}
				
			});
		}


		
	};

	private void showImportMessage(final boolean external) {
		String dir = SDCardUtils.getSDCardRoot(this,external);
		String message = (external == true) ? String.format(getString(R.string.launcher_import_granted),dir) : String.format(getString(R.string.launcher_import_denied),dir);
		Snackbar bar = Snackbar.make(findViewById(R.id.launcher_window_content), message,
				Snackbar.LENGTH_INDEFINITE)
				.setAction(android.R.string.ok,new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						DoImportMenu(external);
					}});

		View snackbarView = bar.getView();
		TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
		textView.setMaxLines(5);  // show multiple line
		bar.show();
	}

	private void showPermissionsMessage(boolean granted) {
		int message = (granted == true) ? R.string.sd_perm_granted : R.string.sd_perm_denies;
		Snackbar bar = Snackbar.make(findViewById(R.id.launcher_window_content), message,
				Snackbar.LENGTH_INDEFINITE)
				.setAction(android.R.string.ok,new View.OnClickListener() {
					@Override
					public void onClick(View view) {

					}});

		View snackbarView = bar.getView();
		TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
		textView.setMaxLines(5);  // show multiple line
		bar.show();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
										   int[] grantResults) {
		boolean external = false;
		if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			external = true;
		}

		switch(requestCode) {
			case RP_INFO:
				showPermissionsMessage(external);
				break;
			case RP_SALVAGE:
				try {
					DoRecovery(this.getPackageName(), external);
				} catch(Exception e) {
					throw new RuntimeException(e) ;
				}
				break;
			case RP_EXPORT:
					AskExportFileName(external);
				break;
			case RP_IMPORT:
					showImportMessage(external);
			default:
				break;
		}
	}

}
