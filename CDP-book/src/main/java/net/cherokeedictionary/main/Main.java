package net.cherokeedictionary.main;

import java.awt.EventQueue;
import java.io.IOException;

import com.cherokeelessons.gui.MainWindow;
import com.cherokeelessons.gui.MainWindow.Config;

public class Main {
	public static void main(String[] args) throws IOException {
		App app = new App();
		Config config = new Config() {
			
			@Override
			public String getApptitle() {
				return "CDP-book";
			}
			
			@Override
			public Thread getApp(String... args) throws Exception {
				return app;
			}
		};
		EventQueue.invokeLater(new MainWindow(config, args));
	}
}
