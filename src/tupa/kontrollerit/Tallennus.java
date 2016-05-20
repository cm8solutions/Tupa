package tupa.kontrollerit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import tupa.Tupa;
import tupa.data.Kohde;
import tupa.data.Turnaus;
import tupa.data.Sarja;
import tupa.data.Tuomari;
import tupa.data.Pelaaja;
import tupa.data.Toimihenkilo;
import tupa.data.Joukkue;

/**
 *
 * @author Marianne & Victor
 */
public class Tallennus {

    private Tupa ikkuna;
    private List<Kohde> kohdetk = new ArrayList<>();
    private Connection con = null;
    private Statement st = null;
    private Statement st2 = null;
    private Statement st3 = null;
    private Statement st4 = null;
    private Yhteys yhteys = new Yhteys();
    private int turnaus_id;
    private String sql = "";
    private boolean poisto = true;

    public Tallennus() {

    }

    public Tallennus(Tupa ikkuna) {

        this.ikkuna = ikkuna;
    }

    public void suoritaTallennus() throws InstantiationException, SQLException, IllegalAccessException {

        kohdetk = ikkuna.annaKohteet();

        try {
            con = yhteys.annaYhteys();
            st = con.createStatement();
             st2 = con.createStatement();
              st3 = con.createStatement();
               st4 = con.createStatement();

            if (poisto) {

                Turnaus turnaus = (Turnaus) ikkuna.annaTurnaus();
                turnaus_id = turnaus.annaID();

                String turnaus_nimi = turnaus.toString();

                ResultSet turnaukset = st.executeQuery("SELECT * FROM  turnaus");
                boolean loyty = false;
                while (turnaukset.next()) {

                    int tid = turnaukset.getInt("id");

                    if (tid == turnaus_id) {
                        loyty = true;
                        break;
                    }

                }

                //ei ollut kannassa ennestään
                if (!loyty) {

                    st.executeUpdate("INSERT INTO turnaus (id, nimi) VALUES('" + turnaus_id + "', '" + turnaus_nimi + "')");

                } //oli jo kannassa, jolloin kaikki siihen liittyvät tiedot tyhjennetään ennen tallentamista
                else {
                    st.executeUpdate("UPDATE turnaus SET nimi='" + turnaus_nimi + "' WHERE id='" + turnaus_id + "'");

                    if (poisto) {
                        
                        
                        //tyhjennetään pelaajat
                        sql = "SELECT DISTINCT pelaaja.id as pid FROM sarja, joukkue, pelaaja WHERE sarja.turnaus_id='" + turnaus_id + "' AND joukkue.sarja_id = sarja.id AND pelaaja.joukkue_id = joukkue.id";
                        ResultSet pelaajat = st.executeQuery(sql);

                        while (pelaajat.next()) {

                            int id = pelaajat.getInt("pid");
                            sql = "DELETE FROM pelaaja WHERE id='" + id + "'";
                            st.executeUpdate(sql);
                        }

                        //tyhjennetään toimarit
                        sql = "SELECT DISTINCT toimari.id as tid FROM sarja, joukkue, toimari WHERE sarja.turnaus_id='" + turnaus_id + "' AND joukkue.sarja_id = sarja.id AND toimari.joukkue_id = joukkue.id";
                        ResultSet toimarit = st2.executeQuery(sql);

                        while (toimarit.next()) {

                            int id = toimarit.getInt("tid");
                            sql = "DELETE FROM toimari WHERE id='" + id + "'";
                            st.executeUpdate(sql);
                        }

                        //MUIDEN TYHJENNYS!!
                        
                             //tyhjennetään joukkueet
                        sql = "SELECT DISTINCT joukkue.id as jid FROM sarja, joukkue WHERE sarja.turnaus_id='" + turnaus_id + "' AND joukkue.sarja_id = sarja.id";
                        ResultSet joukkueet = st3.executeQuery(sql);

                        while (joukkueet.next()) {

                            int id = joukkueet.getInt("jid");
                            sql = "DELETE FROM joukkue WHERE id='" + id + "'";
                            st.executeUpdate(sql);
                        }
                        
                        
                        
                          //tyhjennetään sarjat
                        sql = "DELETE FROM sarja WHERE turnaus_id='" + turnaus_id + "'";
                        st.executeUpdate(sql);
                        
                        
                        
                        
                        //tyhjennetään tuomarit
                        sql = "DELETE FROM tuomari WHERE turnaus_id='" + turnaus_id + "'";
                        st.executeUpdate(sql);
                        
           

                    }

                }

                //ja täytetään se kohdetk:n mukaiseksi
                if (!kohdetk.isEmpty()) {

                    for (int i = 0; i < kohdetk.size(); i++) {

                        Kohde tiedot = kohdetk.get(i);

                        if (tiedot instanceof Sarja) {

                            Sarja sarja = (Sarja) tiedot;
                            int sid = sarja.annaID();
                            String snimi = sarja.toString();

                            st.executeUpdate("INSERT INTO sarja (id, nimi, turnaus_id) VALUES('" + sid + "', '" + snimi + "', '" + turnaus_id + "')");

                        } else if (tiedot instanceof Tuomari) {

                            Tuomari tuomari = (Tuomari) tiedot;

                            int tuomari_id = tuomari.annaJulkinenId();
                            int tid = tuomari.annaID();
                            String etunimi = tuomari.annaEtuNimi();
                            String sukunimi = tuomari.annaSukuNimi();
 
                            st.executeUpdate("INSERT INTO tuomari (id, etunimi, sukunimi, tuomari_id, turnaus_id) VALUES('" + tid + "', '" + etunimi + "', '" + sukunimi + "', '" + tuomari_id + "', '" + turnaus_id + "')");

                        } else if (tiedot instanceof Joukkue) {

                          Joukkue joukkue = (Joukkue) tiedot;
                           int jid = joukkue.annaID();
                         int sarja_id = joukkue.annaSarja().annaID();
                          String jnimi = joukkue.toString();

                           st.executeUpdate("INSERT INTO joukkue (id, nimi, sarja_id) VALUES('" + jid + "', '" + jnimi + "', '" + sarja_id + "')");

                        } else if (tiedot instanceof Pelaaja) {

                            Pelaaja pelaaja = (Pelaaja) tiedot;
                            int pelaaja_id = pelaaja.annaJulkinenID();
                            int pid = pelaaja.annaID();
                            String petunimi = pelaaja.annaEtuNimi();
                            String psukunimi = pelaaja.annaSukuNimi();
                            String pelipaikka = pelaaja.annaPelipaikka();
                            int nro = pelaaja.annaPelinumero();
                            int joukkue_id = pelaaja.annaJoukkue().annaID();
 
                            st.executeUpdate("INSERT INTO pelaaja (id, etunimi, sukunimi, pelipaikka, pelinumero, pelaaja_id, joukkue_id) VALUES('" + pid + "', '" + petunimi + "', '" + psukunimi + "', '" + pelipaikka + "', '" + nro + "', '" + pelaaja_id + "', '" + joukkue_id + "')");

                        } else if (tiedot instanceof Toimihenkilo) {

                            Toimihenkilo toimari = (Toimihenkilo) tiedot;
                            String puh = toimari.annaPuh();
                            int toid = toimari.annaID();
                            String tetunimi = toimari.annaEtuNimi();
                            String tsukunimi = toimari.annaSukuNimi();

                            String rooli = toimari.annaRooli();
                            String sposti = toimari.annaSposti();
                            int tjoukkue_id = toimari.annaJoukkue().annaID();
 
                            st.executeUpdate("INSERT INTO toimari (id, etunimi, sukunimi, rooli, puh, sposti, joukkue_id) VALUES('" + toid + "', '" + tetunimi + "', '" + tsukunimi + "', '" + rooli + "', '" + puh + "', '" + sposti + "', '" + tjoukkue_id + "')");

                        }

                    }

                }
            } //JOS POISTOA EI OLLA TEHTY, päivitetään ainostaan tiedot (tätä käytetään hakutoiminnon yhteydessä), tän voi tehdä vain niille, joita haetaan!
            else {

                Turnaus turnaus = (Turnaus) ikkuna.annaTurnaus();
                turnaus_id = turnaus.annaID();

                String turnaus_nimi = turnaus.toString();

                st.executeUpdate("UPDATE turnaus SET nimi='" + turnaus_nimi + "' WHERE id='" + turnaus_id + "'");

                //ja täytetään se kohdetk:n mukaiseksi
                if (!kohdetk.isEmpty()) {

                    for (int i = 0; i < kohdetk.size(); i++) {

                        Kohde tiedot = kohdetk.get(i);

                        if (tiedot instanceof Sarja) {

                            Sarja sarja = (Sarja) tiedot;

                            int id = sarja.annaID();
                            String nimi = sarja.toString();

                            st.executeUpdate("UPDATE sarja SET nimi='" + nimi + "' WHERE id='" + id + "' AND turnaus_id='" + turnaus_id + "'");

                        } else if (tiedot instanceof Tuomari) {

                            Tuomari tuomari = (Tuomari) tiedot;

                            int tuomari_id = tuomari.annaJulkinenId();
                            int id = tuomari.annaID();
                            String etunimi = tuomari.annaEtuNimi();
                            String sukunimi = tuomari.annaSukuNimi();

                            st.executeUpdate("UPDATE tuomari SET etunimi='" + etunimi + "' WHERE id='" + id + "' AND turnaus_id='" + turnaus_id + "'");
                            st.executeUpdate("UPDATE tuomari SET sukunimi='" + sukunimi + "' WHERE id='" + id + "' AND turnaus_id='" + turnaus_id + "'");

                        } else if (tiedot instanceof Joukkue) {

                            Joukkue joukkue = (Joukkue) tiedot;
                            int id = joukkue.annaID();
                            int sarja_id = joukkue.annaSarja().annaID();
                            String nimi = joukkue.toString();

                            st.executeUpdate("UPDATE joukkue SET nimi='" + nimi + "' WHERE id='" + id + "' AND sarja_id='" + sarja_id + "'");

                        } else if (tiedot instanceof Pelaaja) {

                            Pelaaja pelaaja = (Pelaaja) tiedot;
                            int pelaaja_id = pelaaja.annaJulkinenID();
                            int pid = pelaaja.annaID();
                            String petunimi = pelaaja.annaEtuNimi();
                            String psukunimi = pelaaja.annaSukuNimi();
                            String pelipaikka = pelaaja.annaPelipaikka();
                            int nro = pelaaja.annaPelinumero();
                            int joukkue_id = pelaaja.annaJoukkue().annaID();

                            st.executeUpdate("UPDATE pelaaja SET etunimi='" + petunimi + "' WHERE id='" + pid + "' AND joukkue_id='" + joukkue_id + "'");
                            st.executeUpdate("UPDATE pelaaja SET sukunimi='" + psukunimi + "'  WHERE id='" + pid + "' AND joukkue_id='" + joukkue_id + "'");
                            st.executeUpdate("UPDATE pelaaja SET pelipaikka='" + pelipaikka + "'  WHERE id='" + pid + "' AND joukkue_id='" + joukkue_id + "'");
                            st.executeUpdate("UPDATE pelaaja SET pelinumero='" + nro + "'  WHERE id='" + pid + "' AND joukkue_id='" + joukkue_id + "'");

                        } else if (tiedot instanceof Toimihenkilo) {

                            Toimihenkilo toimari = (Toimihenkilo) tiedot;
                            String puh = toimari.annaPuh();
                            int toid = toimari.annaID();
                            String tetunimi = toimari.annaEtuNimi();
                            String tsukunimi = toimari.annaSukuNimi();

                            String rooli = toimari.annaRooli();
                            String sposti = toimari.annaSposti();
                            int tjoukkue_id = toimari.annaJoukkue().annaID();

                            st.executeUpdate("UPDATE toimari SET etunimi='" + tetunimi + "' WHERE id='" + toid + "' AND joukkue_id='" + tjoukkue_id + "'");
                            st.executeUpdate("UPDATE toimari SET sukunimi='" + tsukunimi + "'  WHERE id='" + toid + "' AND joukkue_id='" + tjoukkue_id + "'");
                            st.executeUpdate("UPDATE toimari SET rooli='" + rooli + "'  WHERE id='" + toid + "' AND joukkue_id='" + tjoukkue_id + "'");
                            st.executeUpdate("UPDATE toimari SET sposti='" + sposti + "'  WHERE id='" + toid + "' AND joukkue_id='" + tjoukkue_id + "'");
                            st.executeUpdate("UPDATE toimari SET puh='" + puh + "'  WHERE id='" + toid + "' AND joukkue_id='" + tjoukkue_id + "'");

                        }

                    }

                }
            }

        } catch (SQLException se) {

            se.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        } finally {

            try {
                if (st != null) {
                    con.close();
                }
            } catch (SQLException se) {
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        //päivitetään tilanne, että tallennus on suoritettu
        ikkuna.asetaMuutos(false);

    }

    public void asetaPoisto(boolean poisto) {
        this.poisto = poisto;
    }

}
