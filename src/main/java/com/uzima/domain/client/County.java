package com.uzima.domain.client;

/** The 47 counties of Kenya, with their official codes (001-047). */
public enum County {
  MOMBASA(1, "Mombasa"),
  KWALE(2, "Kwale"),
  KILIFI(3, "Kilifi"),
  TANA_RIVER(4, "Tana River"),
  LAMU(5, "Lamu"),
  TAITA_TAVETA(6, "Taita-Taveta"),
  GARISSA(7, "Garissa"),
  WAJIR(8, "Wajir"),
  MANDERA(9, "Mandera"),
  MARSABIT(10, "Marsabit"),
  ISIOLO(11, "Isiolo"),
  MERU(12, "Meru"),
  THARAKA_NITHI(13, "Tharaka-Nithi"),
  EMBU(14, "Embu"),
  KITUI(15, "Kitui"),
  MACHAKOS(16, "Machakos"),
  MAKUENI(17, "Makueni"),
  NYANDARUA(18, "Nyandarua"),
  NYERI(19, "Nyeri"),
  KIRINYAGA(20, "Kirinyaga"),
  MURANGA(21, "Murang'a"),
  KIAMBU(22, "Kiambu"),
  TURKANA(23, "Turkana"),
  WEST_POKOT(24, "West Pokot"),
  SAMBURU(25, "Samburu"),
  TRANS_NZOIA(26, "Trans Nzoia"),
  UASIN_GISHU(27, "Uasin Gishu"),
  ELGEYO_MARAKWET(28, "Elgeyo-Marakwet"),
  NANDI(29, "Nandi"),
  BARINGO(30, "Baringo"),
  LAIKIPIA(31, "Laikipia"),
  NAKURU(32, "Nakuru"),
  NAROK(33, "Narok"),
  KAJIADO(34, "Kajiado"),
  KERICHO(35, "Kericho"),
  BOMET(36, "Bomet"),
  KAKAMEGA(37, "Kakamega"),
  VIHIGA(38, "Vihiga"),
  BUNGOMA(39, "Bungoma"),
  BUSIA(40, "Busia"),
  SIAYA(41, "Siaya"),
  KISUMU(42, "Kisumu"),
  HOMA_BAY(43, "Homa Bay"),
  MIGORI(44, "Migori"),
  KISII(45, "Kisii"),
  NYAMIRA(46, "Nyamira"),
  NAIROBI(47, "Nairobi");

  private final int code;
  private final String label;

  County(int code, String label) {
    this.code = code;
    this.label = label;
  }

  public int code() {
    return code;
  }

  public String label() {
    return label;
  }

  @Override
  public String toString() {
    return label;
  }
}
