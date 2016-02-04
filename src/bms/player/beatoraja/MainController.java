package bms.player.beatoraja;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import javax.swing.JFileChooser;

import bms.model.BMSDecoder;
import bms.model.BMSModel;
import bms.model.BMSONDecoder;
import bms.player.beatoraja.audio.AudioProcessor;
import bms.player.beatoraja.audio.SoundProcessor;
import bms.player.beatoraja.bga.BGAManager;
import bms.player.beatoraja.decide.MusicDecide;
import bms.player.beatoraja.result.MusicResult;
import bms.player.beatoraja.select.MusicSelector;
import bms.player.lunaticrave2.LunaticRave2SongDatabaseManager;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter.OutputType;

public class MainController extends ApplicationAdapter {

	private BMSPlayer player;
	private MusicDecide decide;
	private MusicSelector selector;
	private MusicResult result;
	
	private ApplicationAdapter current;
	
	private Config config;
	private int auto;
	
	private File f;

	public MainController(File f, Config config, int auto) {
		this.auto = auto;
		this.config = config;
		this.f = f;
	}
	
	public static final int STATE_SELECTMUSIC = 0;
	public static final int STATE_DECIDE = 1;
	public static final int STATE_PLAYBMS = 2;
	public static final int STATE_RESULT = 3;
	
	public void changeState(int state, PlayerResource resource) {
		switch(state) {
		case STATE_SELECTMUSIC:
			if(this.f != null) {
				exit();
			}
			selector = new MusicSelector(this, config);
			selector.create();
			current = selector;
			break;
		case STATE_DECIDE:
			decide = new MusicDecide(this, resource);
			decide.create();
			current = decide;
			break;
		case STATE_PLAYBMS:
			player = new BMSPlayer(this,resource);
			player.create();
			current = player;
			break;
		case STATE_RESULT:
			result = new MusicResult(this);
			result.create();
			current = result;
			break;
		}
	}
	
	public void setAuto(int auto) {
		this.auto = auto;
	}

	@Override
	public void create() {
		if(f != null) {
			PlayerResource resource = new PlayerResource();
			resource.setBMSFile(f, config, auto);
			changeState(STATE_PLAYBMS, resource);			
		} else {
			changeState(STATE_SELECTMUSIC, null);			
		}
	}

	@Override
	public void render() {
		current.render();
	}

	@Override
	public void dispose() {
		if(player != null) {
			player.dispose();
		}
		if(selector != null) {
			selector.dispose();
		}
		if(decide != null) {
			decide.dispose();
		}
		if(result != null) {
			result.dispose();
		}	
	}

	@Override
	public void pause() {
		current.pause();
	}

	@Override
	public void resize(int width, int height) {
		current.resize(width, height);
	}

	@Override
	public void resume() {
		current.resume();
	}

	public static void main(String[] args) {
		Logger logger = Logger.getGlobal();
		try {
			logger.addHandler(new FileHandler("beatoraja_log.xml"));
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File f = null;
		int auto = 0;
		for (String s : args) {
			if (s.startsWith("-")) {
				if (s.equals("-a")) {
					auto = 1;
				}
				if (s.equals("-r")) {
					auto = 2;
				}
			} else {
				f = new File(s);
			}
		}
		MainController.play(f, auto, true);
	}

	public static void play(File f, int auto, boolean forceExit) {
		Config config = new Config();
		if (new File("config.json").exists()) {
			Json json = new Json();
			try {
				config = json.fromJson(Config.class, new FileReader(
						"config.json"));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} else {
			Json json = new Json();
			json.setOutputType(OutputType.json);
			try {
				FileWriter fw = new FileWriter("config.json");
				fw.write(json.prettyPrint(config));
				fw.flush();
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(config.getBmsroot().length == 0) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setDialogTitle("Choose BMS root directory");
			if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				File[] files = new File[]{chooser.getSelectedFile()};
				String[] rootdir = new String[]{files[0].getAbsolutePath()};
				config.setBmsroot(rootdir);
				Json json = new Json();
				json.setOutputType(OutputType.json);
				try {
					FileWriter fw = new FileWriter("config.json");
					fw.write(json.prettyPrint(config));
					fw.flush();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					LunaticRave2SongDatabaseManager songdb = new LunaticRave2SongDatabaseManager(
							new File("song.db").getPath(), true);
					songdb.createTable();
					Logger.getGlobal().info("song.db更新開始");
					songdb.updateSongDatas(files, rootdir, new File(".").getAbsolutePath());
					Logger.getGlobal().info("song.db更新完了");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

			} else {
				System.exit(1);
			}
		}

		try {
			MainController player = new MainController(f, config, auto);
			LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
			cfg.width = 1280;
			cfg.height = 720;

			// fullscreen
			cfg.fullscreen = config.isFullscreen();
			// vSync
			cfg.vSyncEnabled = config.isVsync();
			if (!config.isVsync()) {
				cfg.backgroundFPS = 0;
				cfg.foregroundFPS = 0;
			}
			cfg.title = "Beatoraja";

			cfg.audioDeviceBufferSize = 384;
			cfg.audioDeviceSimultaneousSources = 64;
			cfg.forceExit = forceExit;

			new LwjglApplication(player, cfg);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.getGlobal().severe(
					e.getClass().getName() + " : " + e.getMessage());
		} catch (Error e) {
			e.printStackTrace();
			Logger.getGlobal().severe(
					e.getClass().getName() + " : " + e.getMessage());
		}
	}
	
	public void exit() {
		Gdx.app.exit();
	}
	
	/**
	 * プレイヤーのコンポーネント間でデータをやり取りするためのクラス
	 * 
	 * @author exch
	 */
	public static class PlayerResource {
		private BMSModel model;
		private Config config;
		private int auto;
		private AudioProcessor audio;
		private BGAManager bga;

		private boolean finished = false;
		
		public void setBMSFile(final File f, final Config config, int autoplay) {
			this.config = config;
			this.auto = autoplay;
			if (f.getPath().toLowerCase().endsWith(".bmson")) {
				BMSONDecoder decoder = new BMSONDecoder();
				model = decoder.decode(f);
			} else {
				BMSDecoder decoder = new BMSDecoder();
				model = decoder.decode(f);
			}
			
			audio = new SoundProcessor();
			bga = new BGAManager(config);
			Thread medialoader = new Thread() {
				@Override
				public void run() {
					try {
						if (config.getBga() == Config.BGA_ON || (config.getBga() == Config.BGA_AUTO && (auto != 0))) {
							bga.setModel(model, f.getPath());
						}
						audio.setModel(model, f.getPath());
					} catch (Exception e) {
						Logger.getGlobal().severe(e.getClass().getName() + " : " + e.getMessage());
						e.printStackTrace();
					} catch (Error e) {
						Logger.getGlobal().severe(e.getClass().getName() + " : " + e.getMessage());
					} finally {
						finished = true;
					}
				}				
			};
			medialoader.start();
		}
		
		public BMSModel getBMSModel() {
			return model;
		}

		public int getAutoplay() {
			return auto;
		}

		public Config getConfig() {
			return config;
		}
		
		public AudioProcessor getAudioProcessor() {
			return audio;
		}
		
		public BGAManager getBGAManager() {
			return bga;
		}
		
		public boolean mediaLoadFinished() {
			return finished;
		}
	}
}