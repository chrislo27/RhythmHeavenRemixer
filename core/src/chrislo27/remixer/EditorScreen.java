package chrislo27.remixer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import chrislo27.remixer.editor.Editor;
import chrislo27.remixer.stage.EditorStageSetup;
import chrislo27.remixer.track.Remix;
import ionium.screen.Updateable;
import ionium.stage.Stage;
import ionium.util.i18n.Localization;

public class EditorScreen extends Updateable<Main> {

	private static Vector3 tmpVec3 = new Vector3();

	private EditorStageSetup stageSetup;
	private Stage stage;

	public Editor editor;

	private int newVersionAvailable = -1;

	public EditorScreen(Main m) {
		super(m);

		editor = new Editor(main);
		editor.setRemix(new Remix(120, Editor.TRACK_COUNT));
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// render backgrounds
		main.batch.begin();

		main.batch.setColor(1f, 0.65f, 0.5f, 1);
		Main.fillRect(main.batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		main.batch.setColor(1, 1, 1, 1);

		main.batch.setColor(0.3f, 0.3f, 0.5f, 1);
		Main.fillRect(main.batch, 0, Gdx.graphics.getHeight(), Gdx.graphics.getWidth(), -48);
		main.batch.setColor(1, 1, 1, 1);

		main.batch.end();

		// render editor
		editor.render(main.batch);
		main.batch.setProjectionMatrix(main.camera.combined);

		// dim if confirmation dialog is visible
		if (stageSetup != null && (stageSetup.shouldDim() || stageSetup.isSelectingFile())) {
			main.batch.begin();
			main.batch.setColor(0.25f, 0.25f, 0.25f, 0.75f);
			Main.fillRect(main.batch, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			main.batch.setColor(1, 1, 1, 1);

			if (stageSetup.isSelectingFile()) {
				main.font.setColor(1, 1, 1, 1);
				main.font.draw(main.batch, Localization.get("menu.dialogOpen"),
						Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.5f, 0,
						Align.center, false);
			}

			main.batch.end();
		}

		if (stageSetup != null) stage.render(main.batch);

		main.batch.begin();
		main.font.setColor(1, 1, 1, 1);
		main.font.draw(main.batch, Main.version, Gdx.graphics.getWidth() - 4,
				4 + main.font.getCapHeight(), 0, Align.right, false);

		if (newVersionAvailable != -1) {
			if (newVersionAvailable == 0) {
				main.font.setColor(0.1f, 0.9f, 0.1f, 1);
				main.font.draw(main.batch, Localization.get("editor.upToDate", Main.githubVersion),
						Gdx.graphics.getWidth() - 4, 4 + main.font.getCapHeight() * 2.5f, 0,
						Align.right, false);
			} else if (newVersionAvailable == 1) {
				main.font.setColor(Color.GOLDENROD);
				main.font.draw(main.batch,
						Localization.get("editor.newVersion", Main.githubVersion),
						Gdx.graphics.getWidth() - 4, 4 + main.font.getCapHeight() * 2.5f, 0,
						Align.right, false);
			}
			main.font.setColor(1, 1, 1, 1);
		}
		main.batch.end();
	}

	private float getBeatPosX(float beat) {
		float units = (Gdx.graphics.getWidth() / editor.camera.viewportWidth) / editor.camera.zoom;
		float relativeX = (Gdx.graphics.getWidth() * 0.5f)
				+ ((beat - editor.camera.position.x) * (units));

		return relativeX;
	}

	@Override
	public void renderUpdate() {
		if (!stageSetup.shouldDim() && !stageSetup.isSelectingFile()) editor.inputUpdate();
		editor.renderUpdate();
		stageSetup.renderUpdate();

		if (newVersionAvailable == -1 && Main.githubVersion != null) {
			if (Main.githubVersion.equalsIgnoreCase(Main.version)) {
				newVersionAvailable = 0;
			} else {
				newVersionAvailable = 1;
			}
		}
	}

	@Override
	public void tickUpdate() {
	}

	@Override
	public void getDebugStrings(Array<String> array) {
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		if (stageSetup == null) {
			stageSetup = new EditorStageSetup(main, this);
			stage = stageSetup.getStage();
		}

		stage.onResize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer) {
			InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();

			stage.addSelfToInputMultiplexer(plex);
			plex.addProcessor(editor);
		}
	}

	@Override
	public void hide() {
		if (Gdx.input.getInputProcessor() instanceof InputMultiplexer && stage != null) {
			Gdx.app.postRunnable(new Runnable() {

				@Override
				public void run() {
					InputMultiplexer plex = (InputMultiplexer) Gdx.input.getInputProcessor();

					stage.removeSelfFromInputMultiplexer(plex);
					plex.removeProcessor(editor);
				}

			});
		}
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

}
