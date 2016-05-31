package chrislo27.remixer.stage;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;

import chrislo27.remixer.EditorScreen;
import chrislo27.remixer.Main;
import chrislo27.remixer.track.Remix;
import ionium.registry.AssetRegistry;
import ionium.stage.Group;
import ionium.stage.Stage;
import ionium.stage.ui.ImageButton;
import ionium.stage.ui.LocalizationStrategy;
import ionium.stage.ui.TextButton;
import ionium.stage.ui.TextLabel;
import ionium.stage.ui.skin.Palette;
import ionium.stage.ui.skin.Palettes;
import ionium.util.i18n.Localization;

public class EditorStageSetup {

	private final Main main;
	private final EditorScreen editorScreen;

	private Stage stage;

	private Group toolbar;
	private ImageButton saveButton;
	private TextButton currentMusic;

	private Group confirmationGroup;
	private ImageButton confirmationYes;
	private ImageButton confirmationNo;
	private TextLabel confirmationLabel;
	private Runnable confirmationRun;

	private Runnable setInvisible = new Runnable() {

		@Override
		public void run() {
			confirmationGroup.setVisible(false);
			toolbar.setVisible(true);
		}
	};

	public EditorStageSetup(Main main, EditorScreen es) {
		this.main = main;
		this.editorScreen = es;

		create();
	}

	public void renderUpdate() {

	}

	private void create() {
		stage = new Stage();
		Palette palette = Palettes.getIoniumDefault(main.font, main.fontBordered);

		toolbar = new Group(stage);

		// toolbar stuff
		{
			ImageButton newProject = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "newFile")) {

				Runnable run = new Runnable() {

					@Override
					public void run() {
						editorScreen.editor.setRemix(new Remix(120));
					}
				};

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					invokeConfirmation("menu.newWarning", run);
				}

			};

			newProject.getColor().set(0.25f, 0.25f, 0.25f, 1);
			toolbar.addActor(newProject).align(Align.topLeft).setPixelOffset(8, 8, 32, 32);

			ImageButton openProject = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "openFile")) {

				Runnable run = new Runnable() {

					@Override
					public void run() {
					}
				};

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					invokeConfirmation("menu.openWarning", run);
				}

			};

			openProject.getColor().set(0.25f, 0.25f, 0.25f, 1);
			toolbar.addActor(openProject).align(Align.topLeft).setPixelOffset(8 + (32 + 8) * 1, 8,
					32, 32);

			saveButton = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "saveFile")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);
				}

			};

			saveButton.getColor().set(0.25f, 0.25f, 0.25f, 1);
			toolbar.addActor(saveButton).align(Align.topLeft)
					.setPixelOffset(8 + (32 + 8) * 2, 8, 32, 32).setEnabled(false);

			ImageButton playRemix = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "play")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					editorScreen.editor.play();
				}

			};

			playRemix.getColor().set(0, 0.5f, 0.055f, 1);
			toolbar.addActor(playRemix).align(Align.topLeft).setPixelOffset(8 + (32 + 8) * 3, 8, 32,
					32);

			ImageButton pauseRemix = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "pause")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					editorScreen.editor.getRemix().pause();
				}

			};

			pauseRemix.getColor().set(0.75f, 0.75f, 0.25f, 1);
			toolbar.addActor(pauseRemix).align(Align.topLeft).setPixelOffset(8 + (32 + 8) * 4, 8,
					32, 32);

			ImageButton stopRemix = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "stop")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					editorScreen.editor.getRemix().stop();
				}

			};

			stopRemix.getColor().set(242 / 255f, 0.0525f, 0.0525f, 1);
			toolbar.addActor(stopRemix).align(Align.topLeft).setPixelOffset(8 + (32 + 8) * 5, 8, 32,
					32);

			currentMusic = new TextButton(stage, palette, "menu.music") {

				boolean selecting = false;
				JFileChooser fileChooser;

				{
					fileChooser = new JFileChooser();

					fileChooser.setCurrentDirectory(
							new File(System.getProperty("user.home"), "Desktop"));

					fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
					fileChooser.setDialogTitle("Select a music file");
					FileNameExtensionFilter ffef = new FileNameExtensionFilter(
							"Supported sound files (.wav, .ogg, .mp3)", "wav", "ogg", "mp3",
							"wave");
					fileChooser.addChoosableFileFilter(ffef);
					fileChooser.setFileFilter(ffef);
				}

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					if (selecting) return;

					Thread t = new Thread() {

						@Override
						public void run() {
							selecting = true;

							int result = fileChooser.showOpenDialog(null);

							if (result == JFileChooser.APPROVE_OPTION) {
								final File selectedFile = fileChooser.getSelectedFile();
								final FileHandle handle = new FileHandle(selectedFile);

								boolean success = editorScreen.editor.setMusic(handle);
								setLocalizationKey(success ? "menu.music" : "menu.musicInvalid");

							}

							System.gc();

							selecting = false;
						}
					};

					t.setDaemon(true);
					t.start();
				}

			};

			currentMusic.setI10NStrategy(new LocalizationStrategy() {

				@Override
				public String get(String key, Object... params) {
					return Localization.get(key, editorScreen.editor.getMusicFile() == null
							? "nothing" : editorScreen.editor.getMusicFile().name());
				}

			});

			toolbar.addActor(currentMusic).align(Align.topLeft).setPixelOffset(8 + (32 + 8) * 6, 8,
					480, 32);

			ImageButton clearMusic = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "no")) {

				Runnable run = new Runnable() {

					@Override
					public void run() {
						editorScreen.editor.setMusic(null);
					}
				};

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					invokeConfirmation("menu.clearMusicWarning", run);
				}

			};

			clearMusic.getColor().set(0.85f, 0.25f, 0.25f, 1);
			toolbar.addActor(clearMusic).align(Align.topLeft)
					.setPixelOffset(8 + (32 + 8) * 6 + 480 - palette.borderThickness, 8, 32, 32);

			ImageButton exitGame = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "no")) {

				Runnable run = new Runnable() {

					@Override
					public void run() {
						Gdx.app.exit();
					}
				};

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					invokeConfirmation("menu.exitWarning", run);
				}

			};

			exitGame.getColor().set(0.85f, 0.25f, 0.25f, 1);
			toolbar.addActor(exitGame).align(Align.topRight).setPixelOffset(8, 8, 32, 32);

			TextButton interval = new TextButton(stage, palette, "") {

				private int interval = 1;
				private final int[] intervals = { 0, 2, 3, 4, 5, 6, 8, 10 };

				{
					updateIntervalText();
				}

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					interval++;

					if (interval >= intervals.length) {
						interval = 0;
					}

					if (intervals[interval] == 0) {
						editorScreen.editor.lockingInterval = 0;
					} else {
						editorScreen.editor.lockingInterval = 1f / intervals[interval];
					}

					updateIntervalText();
				}

				private void updateIntervalText() {
					switch (intervals[interval]) {
					case 2:
						setLocalizationKey("Snap: 1/2");
						break;
					case 3:
						setLocalizationKey("Snap: 1/3");
						break;
					case 4:
						setLocalizationKey("Snap: 1/4");
						break;
					case 5:
						setLocalizationKey("Snap: 1/5");
						break;
					case 6:
						setLocalizationKey("Snap: 1/6");
						break;
					case 8:
						setLocalizationKey("Snap: 1/8");
						break;
					case 10:
						setLocalizationKey("Snap: 1/10");
						break;
					case 0:
						setLocalizationKey("No snap");
						break;
					}
				}

			};

			interval.setI10NStrategy(new LocalizationStrategy() {

				@Override
				public String get(String key, Object... objects) {
					if (key == null) return "";

					return key;
				}

			});

			toolbar.addActor(interval).align(Align.topRight).setPixelOffset(48, 8, 128, 32);
		}

		stage.addActor(toolbar);

		confirmationGroup = new Group(stage);

		// confirmation group
		{
			confirmationYes = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "yes")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					Gdx.app.postRunnable(setInvisible);
					if (confirmationRun != null) {
						Gdx.app.postRunnable(confirmationRun);
					}
				}

			};

			confirmationYes.align(Align.center).setPixelOffsetSize(64, 64).setScreenOffset(-0.1f,
					-0.1f);
			confirmationYes.getColor().set(0.25f, 0.75f, 0.25f, 1);
			confirmationGroup.addActor(confirmationYes);

			confirmationNo = new ImageButton(stage, palette,
					AssetRegistry.getAtlasRegion("ionium_ui-icons", "no")) {

				@Override
				public void onClickAction(float x, float y) {
					super.onClickAction(x, y);

					Gdx.app.postRunnable(setInvisible);
				}

			};

			confirmationNo.align(Align.center).setPixelOffsetSize(64, 64).setScreenOffset(0.1f,
					-0.1f);
			confirmationNo.getColor().set(0.9f, 0.25f, 0.25f, 1);
			confirmationGroup.addActor(confirmationNo);

			confirmationLabel = new TextLabel(stage, palette, null);
			confirmationLabel.setTextAlign(Align.center);
			confirmationLabel.align(Align.center).setScreenOffset(0, 0.1f, 1, 0.5f);
			confirmationGroup.addActor(confirmationLabel);
		}

		stage.addActor(confirmationGroup).setVisible(false);

	}

	protected void invokeConfirmation(String text, Runnable toRun) {
		confirmationRun = toRun;
		confirmationLabel.setLocalizationKey(text);

		toolbar.setVisible(false);
		confirmationGroup.setVisible(true);
	}

	public Stage getStage() {
		return stage;
	}

	public boolean shouldDim() {
		return confirmationGroup.isVisible();
	}

}
