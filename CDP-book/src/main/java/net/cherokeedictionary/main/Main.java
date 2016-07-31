package net.cherokeedictionary.main;

import java.awt.EventQueue;
import java.io.IOException;

import com.cherokeelessons.gui.MainWindow;
import com.cherokeelessons.gui.MainWindow.Config;

public class Main {
	public static void main(String[] args) throws IOException {
		Config config = new Config() {
			@Override
			public String getApptitle() {
				return "CDP-book";
			}
			
			@Override
			public Thread getApp(String... args) throws Exception {
				return new App();
			}
		};
		EventQueue.invokeLater(new MainWindow(config, args));
	}
}
