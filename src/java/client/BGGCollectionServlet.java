/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author xdr
 */
@WebServlet(name = "BGGCollectionServlet", urlPatterns = {"/BGGCollectionServlet"})
public class BGGCollectionServlet extends HttpServlet {

	private static int numberOfPlayers;
	private static int maxGameTime;

	/**
	 * Processes requests for both HTTP <code>GET</code> and
	 * <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			printHeader(out);
			printFooter(out);
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		try (PrintWriter out = response.getWriter()) {
			printHeader(out);
			printForm(out);
			printFooter(out);
		}

		/**
		 * Handles the HTTP <code>POST</code> method.
		 *
		 * @param request servlet request
		 * @param response servlet response
		 * @throws ServletException if a servlet-specific error occurs
		 * @throws IOException if an I/O error occurs
		 */
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		printHeader(out);
		try {
			numberOfPlayers = Integer.parseInt(request.getParameter("number_of_players"));
			maxGameTime = Integer.parseInt(request.getParameter("max_game_time"));
			String gameOwner = "Dan%20Johansson";
			//JSONArray boardGames = null;
			//JSONArray games = getGames(gameOwner);
			//int numberOfGames = games.length();
			URL url = new URL("http://bgg-json.azurewebsites.net/collection/" + gameOwner);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");

			if (connection.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
					+ connection.getResponseCode());
			}

			InputStream stream = connection.getInputStream();
			DataInputStream httpStream = new DataInputStream(stream);
			BufferedReader httpReader = new BufferedReader(new InputStreamReader(httpStream));
			String result = "{\"boardgames\":" + httpReader.readLine();
			String resultLine;

			while ((resultLine = httpReader.readLine()) != null) {
				result = result + resultLine;
			}
			result = result + ", \"responseStatus\": 200}";

			JSONTokener jsonTokener = new JSONTokener(result);
			JSONObject jsonObject = new JSONObject(jsonTokener);
			JSONArray boardGames = jsonObject.getJSONArray("boardgames");

			int numberOfGames = boardGames.length();
			for (int i = 0; i < numberOfGames; i++) {

				JSONObject game = boardGames.getJSONObject(i);
				if (game.getBoolean("owned")) {
					if (game.getInt("playingTime") <= maxGameTime) {
						if (game.getInt("maxPlayers") >= numberOfPlayers && game.getInt("minPlayers") <= numberOfPlayers) {
							out.println("<div class=\"boardgame\">");
							out.println("<h1 class=\"gameName\">" + game.getString("name") + "</h1>");
							out.println("<a href=" + game.getString("image") + ">"
								+ "<img class=\"thumbnail\" src="
								+ game.getString("thumbnail") + "></a>");
							out.println("<br>");
							if (game.getInt("maxPlayers") == game.getInt("minPlayers")) {
								out.println("<p class=\"gameDetails\">Antal spelare "
									+ game.getInt("maxPlayers") + " st" + "</p>");
							} else {
								out.println("<p class=\"gameDetails\">Antal spelare " + game.getInt("minPlayers") + "-"
									+ game.getInt("maxPlayers") + " personer" + "</p>");
							}
							if (game.getInt("playingTime") < 0) {
								out.println("<p class=\"gameDetails\">Speltid: Okänd" + "</p>");
							}
							if (game.getInt("playingTime") > 0) {
								out.println("<p class=\"gameDetails\">Speltid: "
									+ game.getInt("playingTime") + " minuter" + "</p>");
							}
							out.println("</div>");
						}
					}
				}
			}

			out.println("<br>");
		} catch (MalformedURLException e) {
			out.println("Something is wrong with the url." + "<br>");
		} catch (IOException e) {
			out.println("I/O error" + "<br>");
		} catch (NumberFormatException e) {
			out.println("Du måste fylla i båda fälten med enbart siffror." + "<br>");
		}
		printForm(out);
		printFooter(out);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

	private void printHeader(PrintWriter out) {
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"./css/BGGCollection.css\" />");
		out.println("<title>BGGCollection</title>");
		out.println("</head>");
		out.println("<body>");
		out.println("<div class=\"container\">");
	}

	private void printForm(PrintWriter out) {
		out.println("<div class=\"sokruta\">");
		out.println("<form method = \"post\" >");

		out.println("Antal spelare:  <input type = \"number\" min=\"1\" max=\"362\" required=\"true\" name = \"number_of_players\" value=\""
			+ numberOfPlayers + "\"/>");
		out.println("Maximal speltid: <input type = \"number\" min=\"0\" max=\"240\" required=\"true\"  name = \"max_game_time\" value=\""
			+ maxGameTime + "\"/>");

		out.println("<input type = \"submit\" value = \"Sök spel\" />");
		out.println("</form>");
		out.println("</div>");
	}

	private void printFooter(PrintWriter out) {
		out.println("</div");
		out.println("</body>");
		out.println("</html>");
	}

}
