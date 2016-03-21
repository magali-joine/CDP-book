package net.cherokeedictionary.main;

import java.awt.EventQueue;

import com.newsrx.gui.MainWindow;
import com.newsrx.gui.MainWindow.Config;

public class Main {
	public static void main(String[] args) {
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
