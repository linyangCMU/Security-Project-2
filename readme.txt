README:
Group:
  - Chris Murphy
  - Andrew Lawrence
  - Seth Glickman


Final Design:
  This project implements a man-in-the-middle attack on SSL.  The program is the proverbial man in the middle, as a SSL proxy server.  The user connects to the MITMProxy which then passes on a new request to the intended destination.  When the proxy server has reached the destination, it creates a connection, receives a legitimate certificate, and forges a new certificate to be presented to the user with the legitimate details of the original destination.
  Using keytool, we first created a keystore with a private key with alias "andrew", password "bowdoincs".  Upon every connection attempt, we then use this private key to self-sign our forged certificates which are then given to the user.  All of the traffic between the user and the destination is observed through this proxy server in plain text, and can optionally be logged.
  
  The second part of the project involves a listening server for an authorized user to connect to.  The user can either request the number of connections the proxy has made so far, or terminate the proxy server.
  Users are authorized via an encrypted password file, with the following design.
  There are two files: the password file, and the pepper file.  The pepper file is simpler, and consists of a list of usernames and peppers (random 16-character strings).  The password file has 3 columns: username, salt, and password hash.  We implemented these with customized classes: PasswordFile and PepperFile.  The main contents of each is a HashTable which takes as a key the username, and the value either the pepper String (for the pepper file) or a tuple of (salt, password hash).
  
  In order to create a password hash, you must have a). the user's password, b). the 16-character random salt (located in the password file), and c). the 16-character random pepper (located in the separate pepper file).  Each is encrypted with a different key, and the password file is then MAC'd with a third key and and the MAC is appended to the end of the encrypted password file on disk.
  
  The MITMAdminClient class creates an SSL connection with the MITMAdminServer, and gives a username, password, and command.  If the user is authenticated from the previously-created password file, then the command is run.
 
Steps to run:
    1. Create a private key in a keystore by running `keytool -genkey -keyalg rsa` and inputting an alias and password (we used "andrew" as our alias, and "bowdoincs" as our password, unless otherwise noted).
    2. Create a plaintext password file named "file" in the directory above the mitm directory. Each line should be of the form "username password".
    3. Run PasswordTool to generate the encrypted password and pepper files as follows: `java mitm.PasswordTool file`
    4. Be aware that the original plaintext password file is still around - feel free to copy it to a secure location or delete it at this point.
    5. Run the SSL proxy server with the command `java -classpath ${CLASSPATH}:.:iaik_jce.jar mitm.MITMProxyServer -keyStore [path to keystore] -keyStorePassword bowdoincs -pwdFile [pwdFile]`
    6. Make sure that you have SSL proxy settings on the computer or browser you expect your victim to be using set up to point to localhost:8001.
    7. Visit a https site with the browser.
    8. Watch as the plaintext rolls in.

Names & Passwords:
    We have two different keystores.  One of them takes care of the certificate forging from part 1, while the other handles the user authentication in part 2.
    The username is "andrew", while the password is "bowdoincs".  Other passwords for encrypting specific files are "bowdoincs_[whatever]" where [whatever] is a brief description of what the file is intended to be (e.g. "bowdoincs_pepper").  These can be found hardcoded in the project's source files.
    
Log:
----------------------------------------------------------------------------------------------------------------------------------------------------------------




                                                                     file
                                                                     file
                                                                     Admin server initialized, listening on port 8002
                                                                     --- localhost:55053->www.google.com:443 opened --
                                                                     --- www.google.com:443->localhost:55053 opened --
                                                                     ------ localhost:55053->www.google.com:443 ------
                                                                     GET /accounts/ServiceLogin?service=mail&passive=true&rm=false&continue=http%3A%2F%2Fmail.google.com%2Fmail%2F%3Fui%3Dhtml%26zy%3Dl&bsv=zpwhtygjntrz&scc=1&ltmpl=default&ltmplcache=2 HTTP/1.1
                                                                     User-Agent: Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.10
                                                                     Host: www.google.com
                                                                     Accept: text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1
                                                                     Accept-Language: en,ja;q=0.9,fr;q=0.8,de;q=0.7,es;q=0.6,it;q=0.5,pt;q=0.4,pt-PT;q=0.3,nl;q=0.2,sv;q=0.1,nb;q=0.1,da;q=0.1,fi;q=0.1,ru;q=0.1,pl;q=0.1,zh-CN;q=0.1,zh-TW;q=0.1,ko;q=0.1
                                                                     Accept-Charset: iso-8859-1, utf-8, utf-16, *;q=0.1
                                                                     Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0
                                                                     Referer: http://mail.google.com/
                                                                     Cookie: GoogleAccountsLocale_session=en; GALX=zK93VpRIQt4; __utmz=173272373.1259953716.1.1.utmccn=(direct)|utmcsr=(direct)|utmcmd=(none); __utma=173272373.191646898.1259953716.1260412682.1260504333.3; __utmc=173272373; PREF=ID=291532419a6b3600:U=d01abcb07191b0e7:TM=1259800278:LM=1259800513:S=xFWh6CVa5Wo_vKuQ; TZ=300; GMAIL_RTT=67; NID=29=KmI8BpoSBchC0maHjFQhfWFJ8XttR0J0O0qCR88QegHWx4bixekFavQCadjGxe4bqNNJQPM54qsQWbZSjN0J1TTPWqs-EUUoMmf_gexTtcmLZq5WEZrI6XFoWKHpmRmZ
                                                                     Cookie2: $Version=1
                                                                     Connection: Keep-Alive, TE
                                                                     TE: deflate, gzip, chunked, identity, trailers


                                                                     ------ www.google.com:443->localhost:55053 ------
                                                                     HTTP/1.1 200 OK
                                                                     Content-Type: text/html; charset=UTF-8
                                                                     Cache-control: no-cache, no-store
                                                                     Pragma: no-cache
                                                                     Expires: Mon, 01-Jan-1990 00:00:00 GMT
                                                                     Set-Cookie: GALX=zK93VpRIQt4;Path=/accounts;Secure
                                                                     Content-Encoding: gzip
                                                                     Date: Fri, 11 Dec 2009 05:10:37 GMT
                                                                     X-Content-Type-Options: nosniff
                                                                     X-XSS-Protection: 0
                                                                     Content-Length: 5469
                                                                     Server: GFE/2.0


                                                                     ------ www.google.com:443->localhost:55053 ------
                                                                     [1F8B0800000000000000ED]<is[DB]8[B2DFF5]+[10A6]lI[CFBA0FDBB2]L[A79CC399D4]f[92]l[E2EC]N[BD94]K[0591A0C4]1[AF]%A)J[C6FFFD]u[03E0])J[F6E498DA]W5[99A988041A8DEE]F_[009AA99D]/[B9EB1087]z[0B]]c[9E]FL;[D4]5[8787DA]E[ED]|[C9A809]?.[E394],9[0FDAEC]?[B1BDD2B5]g[BEC799C7DBD79B80]i[C490]o[BAC6D9]g[DE]EtSb,i[18]1[AE7FBCBE]j[9F]"[AA88]o[1C]F8[0CD0059811]E[D0FAA8DDAECD]}s[D3E2]f[CBB4]W[ADA0]E[[16A0]kE[01F5C8]W|l[[D4B59DCD19A1A1]M[9D]VD[BDA81DB1D0B6EEC4]H[F2D5A5E1C2F6DADC0FCE06]w[B5]Z[C7] _[D7B6C997]gd4%Kf/[96FC8CF4B0]g[1ECFE7]@[C4D7]95n[17A11F]{f[DBF01D]?<{[FC]l[F8]|ru[85]0[DC]!_[03]j[9AB6B780]QS[92A1]B[B2DBD4B117DE19]q[98C5A7]d[C5]Bn[1BD4]IZ[8184];[C010]>[04]C[88]t[ED]B1[FF0E]"[E6]>[E7BE8B]H[BE878E040B889306DCF6]a1[94A8]z[E20FA0][[DA9CB5]a[9D0C]v[E6F9EB9006]S[9209F6ECF18BD317]/[AE].[0B9319A0]%,D[A4961FBA]m[CFB7]=[139A]*[1684]<[B6C41F]@[E987]&[C306B9]D[A41F]|&[91EFD8A6]@[C3]([8F]C[D6B6]][BA]`9n[FB]c[D2C3FF]*[E5][[C9FF]][86CC]d[9111DA8AE9]2[CAFE]Cp[CA85]A[02]#x[8F83D99CA3FCE2]0B[D602]`[1BC4]0%Rq[01]u[0F]x[EA91F6]@[FC16]QI[9109FA]2\3[C7F6]n[9305]![8FC5]rT
                                                                     [DAD9]f2[C30F])rxF<[DF]cS[92])
                                                                     9[09]>O[89B0BCB5]2[9EB9EF98AA])[B2BF]0[A0]x[84]0y
                                                                      [89]
                                                                      [D71FDEBD7F]u[FD]b[96101C83]*y[B3]E[C8]63[D0EBBC92]x~;d[01C89DC4A1D3]@[0F139D]u[BB].[B59DCEC2F7170EEB18BE]+[DEBB]K[E604]][B1CE]Q[17]Q[8118]:[81B7]h[CA]eV[BA]-[E9]Jl~4[06]y[DEED]#[C7FDF1E4B49106]$[E9E1]T[84]?[818AE1F1]X[9291]Hf\![99]l[A90CE6]8y3[C4F5FC]&*[1C1F]Tz[A6]L)[9A]EA[08]:[92],[14C8]f [14]Gy[A2]A[81]&[F1]f9>[E5CA]*[F3]Rc44[96]3[8FAD0B]T[FE1C]"{[BD]o$2[A0EE]_C[E2F8F41B])[E4]`[F60BF6D309EC]}[0B]q[16]c[E6CC]6[D0EBFE]tE,[E8]a[FF]8O#[BEE5BD18]xJ[8F85B3B9F3E3A4]&Q[EE11]ZAf[A9];[16BE]wP[F0]&[09]q[E1CF]%[AE]=*8[93]?M[1FFFE9C2]k[8FF216B18FBE]*[F2FE]
                                                                     [F1]=[9C]B[E59BBB]][D2]nC[8EDC15A9]3<p[9BE3]/!/q[EE]3[F2027F8815FA].y)[E802]X[05]s[DE]U[A9BB]H[91E70B91]*[E8]*[A9]"[98]>[E8]"[918384]jUx[F3]=0NS[D7DE]z[AFE1B7D19C12]L[DE]9[C5BC]Y[D0AE]O[C607]*)[D3]{D$[17BACC]-[08061195]G@[17BE]an [DE90EA]s[10F3]J[0E808C]I[B698]
                                                                     i[FF]@[BCDBEE82]D[A1A1]k[7F]F[E8FEA0B3B0]--#J[A11C]
                                                                     [9584F5F1040885FD89909BA688C6]$[8DAC]D[F6A2F7]{]1[7F979B]E[BA]&[9383]-[E995]y[C8C906]B[C7]AA[0CFD12E3]2[AD]Vs\[9CC396]GQ#[9CE1C5A1]7[8F82E9]y[17DA]/Rb[BA]<TX[12DA]d[AEA5CB9F]j"[1EBA]0[19]w*[BF16CAA6AB]=[95]$[F7]l\[9ADF]ph[14E99ADA]N[F192]H3[F108FE14]p[D9]!h[A4]{[9197B704]S(C[7F8D1B]@}[A0C8D1F21B]A[B10F9CE6]r[[91]n[E713]V[91AF]&[19]-[E4EDDAC5F9FCE2DFCC01]
                                                                     [02]r}i?[E7DD]y[0501])S[A196]_[9907]r[1516B8DA]^[B8E21CF3]J[C1])[15D9]=[CB]|[87EC]R[B4D5A4DF8FB79AFAAED0AD82]y[DCD795]v[141DC7]h[8F]Z[8E0B]j[D9]/[C9]-[93]~[A210A5E517]+|[A9DC] [A1]A[10FAD4]X[E2]Z3[E1]Bp[B1B7]p)s[AB]^*I[F309]8[BB92BDA7]$[14]m[04C2E3]6M[A894049DBADEEEA78E9BD81199C7B6C3C1D912BE]d[C4]6[19850788]7[82]XbP[8FCC19]q[FD10FA]<[1EDBDC]^[B116]a[96]e[1B]6[88AE]E[A8]g[92]8bV[EC]t[C8]%<[BB]t[0303D88A]y[C48A]=h[B3]P[BED4]qZj[CA]%[8DCE]`Y[82]tI[94B3D4]zZa[1992F7]d![F0]]JB[9BF40E80]#[8E01A5]Z[AB0B9B]q[AD]J[D19204]y[87FE]Vl[C0]5[01]R[10]#,[F4]k[16]E[0491091B9E0B]B[FEC1]X@boMA[B5]@ [0081F181F8]1[88D9]"[1B]?[0E]A[96]s[FF]3[B0C397]JQ[EA11B4]y[FE8AA280]agl,=[F0858B]
                                                                     [A80B]NY[A9F3DFC0]6
                                                                     [F4]{Y[FED59FDBA8D986019CE5D8]~[0F]a^[AD]1h[93]`[D395A0C112]6[F6]d[BE91E7]
                                                                     [B098B2]W4[03E3]k6[07]O[01]~[161405CC04A6C0980B]!w![0C0683]-[9811CED4]![E794],Cf[C9B00C] [EBF5BA1094C584]][07B412A691]1[1B]O[011FC7DC9D]E0)DX[81]u[19041A],[1E]
                                                                     =[A1DCE7]]z[F1]c%[ADF6]7[DFAD]_>[8F]PkDr[9013F6DB1508EB]\[1C]M[DAA6FE9FD8E7F4]bp2>[E90C]N[06FDE311]dj[D0]u[01CAB7A0F3]
                                                                     [07ED]k[A0A11A90]T[A2FC9B88D2]
                                                                     [19]#[8ACC9D]j[067F0B]3[AB]r[B0]E[0F86]-[8FDA]m"rO[82EA8D])#4[A2]l[80]F[D18EA7AF82E10B]q[DE]Z[0317]a[88D3AD05B5E9CC87]L[0F80]>[C4]s[D7E68D]&[F9]Z[83F1F0BF]m[91C6DAF6]L[1FD61AE08C]y[11120109FC0919C8]T[A2]*[83]@[F608] w[84]9[11]+B[F3]0f[A2AF]V[C3]L[B7]HP[C4F895]o[C4]QCN[B1A2]![B188]N[BCD8]q[A68A].[13FA]][F0849D05E3]/[1C868F]O7[AF]L[18]@[C4]4[08BF03A6A189]I[84]`[F014]R[CB13]Y[E2]9[83]*`[AE8490]X[14]}V"[1DF1D29189FAE1]a[FADC81]%[8C19D1]%W[E48F]?[C8]V[87A6]5[13]$$[EDB5A4]\[A6A2]9G[B3D5]y[071ABE]6[F3]#dKy[8810FA]]
                                                                     [B404B4]U*E[E1]l^K[0EE7]5[A9],[8480]&uX[18028BD102A4A00E1D]CV<[118C]\[88]6x[BEB895A9E5]N[ECF1]t[9008881F881194]Ur[A3]vI{[9811]K[D601AFE5E0]n[BE8098]\
                                                                     [CCE417E6AC189EEEB6]H~[96]*[BAEE]2|V[B0]6[BF19E1]I[EFA080CC]X[C6]Q[F8C3B081]B[FD]PV[E5C1E50F]E[19]2[F7]G[E0]S[08F1BEC2]XR[0C]@[1DF4F4]e[D4]R[85EEC7]G*[F019].[FF]Ft[B2]
                                                                     U[F38CD81C1CB8]![8DA1A8BBE8]H[D0]m[97]}[94]2j[F5]C
                                                                     ------ www.google.com:443->localhost:55053 ------

                                                                     @w[99]m[93]K[0119B2048C]9Q[F7030B]W[B6C184]O[BE8CF9F2]I$[1B]t[B911]v[19]_[FA]0[[E0]G[BC]4[87EF]E[C287EBE99CE91F]M:[F1]FU[F4]hN[B5AD01C0978A]F9[B6] Te[070B]I|[CE]_
                                                                     [95F2D0]a)O[1DA7]y)nw[B5]\>[[BD]u[D0] Zj[159B07B915D192]-[B5]&[7F]3&[92EDB1F6989D]2fQ[89DEF602C8]+[A58F]Y[DAA689F7A61E]u[19]^[9CBA81]S[9281F0E9BA]f[C2F0D8E10FC2]`[C0EE85]U[A319]hi[80]Wr[1302]}*[049A1E05A4D216AFC986]PK[D8DD95FEF7B7D2FF]T[9C]"[91F21DB1]/[07]"J[18]Ky[94D6EE8BA60F0003]96d[98C9EE07]~E[068EC9A8C8]k[D214]([CB]u[AA]W[AF]xB$[D51DB6DC]`x[01]
                                                                     E[A0B7AD82C8E4998F809383BB]0zG2[A5B9B00809]\[8E8F]#yxp)-J[9E19EC]L[DAD4A6]W4[A7]i[AC8CB2F980F4];]Q[D99A06]YB[D21CC8F7DE85EC]Y[08B9]*S[B3]6[92B0AE98]+%Q[AAFDAE]V[81]G[18]gy[BCFAC9929D]O[1A1EFB01A4]vC[1EC99C]$[CB]$[D2F914]L[92]K[94]2[BB]J[C288F0]q)[818414F38E]\[86BF]_[B1]r[12DD]>[06]Kl65[0B85]@[9C]~[C8]V[91B2]+kA[AB] `![900388CE8FE013D1F2CEC4]D"[8317]Ox[06]W[D495]{[0C17CB] l/[06]cC[E3]K[DFF21AA9]LXm[A3]*[0F]7[9FC4B68EB1E690BAC1F4CB]F[17];[C7FD13]+[A7].[E7]M^*[A695]N[FF]>l[10]n[04A2],[ECE4]qX[1496FC]~$f[B494]X[F0A102]M[FB]x2[198EC793DEF1]d0[E80F]F[93]a[BF7F]?V[E9]\[05DE]m?[[F2B27F]-[B2C8]0[94F8E1A102D1]>[E68A]^^[E2]{y[F9FAB7]j[F7FFE51F93E1BF82F7AFFEC9]GU8[D1C1]$D[BD900B].[E892CF00]-[DD]Z[FF]TK[10]jr[BB]'&[93]&ROL[04] [EAB2ABBBDB0C0B];mez[F2F8]k[B7E516],[B4D28EF7]Y[ACD8D9]@[00]+[[EC]nC
                                                                     [D408]!R)[1CB9]=[928BA69E0BD2]QR)JF[CB]I&[01D9]'[9A]R[ACD9]'[9D]B[D8]+[CA]g;[08E6]X[83]<[C1B8C5]l*[E18B85911D]a=[D6]3[DFBFB595]W[D8]j[ADE5]Tj[C3]"-[E1]3[CFD3]Nu[AF87EE87A5BFF6EA][[EA]]Z[0387CE9903B9]oXEWI[A4B00B90])[03A71B82]U[12CC84CC]A [15]h[BE]C[CC]yFdZ[BB]5[BBDCD6A4E60C08]^yU[86AC129AED],[B7]Z[11]T[1ED6B682D0]_kU[D1]N]Qi[C3]a[A7B7]E[15EE]+[E5]T[A5FC]-Q[09]y[F8],[11970EE9CAD1]%[8A83C00F]ywn{][C899D6],[EC049B]'[F2]I[1F1D0F]G[C7]"[E4]X[C1C7D0D1C5C6E2]`xy0[B882FF8BDB0B]hH6[18F0]x[E5870B9F]K[0B]:[18]^Y[C1][[CFD91C0C9FF70F06C7]I[0C84]WDx0[18]#[C6]1[E284BF]J[04A6]m[09C4F0]*[B6F1E7]9[86]Cl<[FEB211EFF0]r[ACC21C]`[96]C[8E850F87]W[E5B2]%/[F1F7F112FF17F1B2]t[F4]\[ACE0]4\0[AECFD419E0]3[EA1D]>[1E]N[A6]\[1D10CBA3]^[C5D913A196B4C2E1E48F17CBE9]N9[A715FD]{" [8D]x[949287]Z_hHb
                                                                     b[C2]m[DED6]^7[F6EC15]x[08D8]-[8B]<T[8DFB89FBDC]t[83]{[CF]v[F6EFA4EFEFA4EF]'$}?[8D]9yr[90]a[DC]>IH[CF1110]ab[8E]y[178017]
                                                                     [CC]3[B7]/[15E4]-[88BC]p[B0BDDB]H[F4]5 6[FBEB0CB899]@[E7CF]v[8AF5BEC5]z[84]\[D5]BZ[8C] [8B87C5]
                                                                     [06]N[A4D2A3]r[FF8E0B91]l[0FA8].[9A0B17B6]g[FD1EE9]U[9E]W[BC]a[EBB4]:[E009]y[C5EB91BCAEC1]K[1C]F[A3C2B5A0]b[AE]PJQd[AB]@A[12D8B31A]^
                                                                     ][90E1D8C6ADAEA90B05C7]7DM[AE]^[DFE7]+$[8AFA14]/c[CB]7d{
                                                                     o[B5AA1BB1]=[95B100]O[B7A91695C7DAFE]D#G[A510B3]<[CB00])&[01891C]>[EE9F9E]LUX[FA]Sd[85]Z[B1]@![1FC2]@;[13]
                                                                     [DD]>[88BA]/;[CA1516]y[DCE9]2[C891E6]~[CCC5]=[A6]vq[89CF]I[D908]-[E4]~[AA88]`[D7DFDF]8[EF]l[BDA4]<[F2D8]Z[1180AA99D4]2>J[83]y[AA8D8AEDEA] ^[0CF4A9]q[DF]c[C0]Y[E89F871515]`;[0B]9[86FF1FEB8B]J[951B95]^d[EBA698]([C68EC59F8B]C[C30F]6[D3]A[AF]7I[CA]P[DA15]zW[CE]`[82] JW~[1E]G[B607D99BBA82]O[AFD0]]f[DAB1AB]3~X[BE]Po[A389801C99]'[BA0C]H[1FA8A0]8[F4A3C8A3]+[ED]B[960680FB]%O[15]v[D49E]J[D204C6]9[A8]A[07FF8A029F0B129FE4]&e^{[E1]Z<<[DC]&+[F908]H[F4]'[F3]>[05]D;[E7BBDF14]@[EA]n[A4],[E01A9F95E6B71FBCD9D1].~[0184DB06]Sq[BAF7]w[01]XU[01]X[DE]u[EC]?[C9AEE1]E[FD]SYQ[F2]A[8A7FA67F]}[15159B98]y[969C]P7[9A]_q[08]]p[1D]T[D5]^P[EE879D18]@/[17]x[9ACFFDD7]>lM[9FD188]5[9A]S[84B4A3991FE800DFC1]<[E2F3][[ABA1F9010BA9D6]|[04E69880D8AC08E2]F6[93108787]iM[00EC]3[0E0F1F0984D9B8F1F6]@2~[C8]P[971AA5A1D4]([12B5B8]-[02],[98]q[EB17]A"[AB0812]Q[8B86B682B1AD86] 1[99]Y[FEC2]4[CDAFD0]U[1817]P[C795E68A]ck[EDFE1F7F14FA]C[B6B805D7],[1197FA]d"[121183]Uv[FF]>p[AB];@
                                                                     <[BEF0]+[FBC0C5CE]eGS[DD0F88ED84]`{[A59714A6F3128B]?`[130894],[AEF0];[01]d[AE]%[D7]Bk[A21C]V[8F]tqA[A190AD].[F4]qg|'[05B4B8958289],[A9][[0FC61FAECE]va[EF]wFS[BCE7F8FA]`d[0BEA]0[DFEBEE]BX[EB]w[86]w[8A]`$[B4BC80]@[CBB0D31F]u[FAE3CE]d[D0]9[1E7FB7F0] [C8]8l[CDE6B7]6[DF]I[D4B0]?P$[F9C1]6I[91EF]m[BAE0]W[FBDF]M[8B]0[D8]d%uI[C59F1BBC9385D3BB]-S0[E994]$[B40F8AB48B]K[AA]<[1F]w[ADDAD6B499AF8A]x[D8]
                                                                     [ACCF]R[AD]l[1D]^[D3]i[B0]Y[18A8]@[AFF4].[C0CF]_%[E9081BC5]s[F801B4]
                                                                     [FB08E0];[0EF316]|[D914E3]@a[DFD0]7[8D]U3[E5E5]N= [83]w-[FE]e[16]P[BE]<[D3A6F8A3]w[B5];[1C]U[DA]3t0[1C16]}f[CEDBA481]Qk^[F49A]ei[AB098E]tmj[FA10]I==[17]J[B1]My>C[1CDCEADAF5FFEADA]Q[03]?tz[0E89]}[130BACAE]m[97]}[F1]=[F6D6B2]"[C61BCDA31D]s$[CE]NU[1ECEA2]$*l[A9C0]v[E0]h4[E5]y[7F]Z[B5940B]G[A5]h[A4BE],
                                                                     ------ www.google.com:443->localhost:55053 ------
                                                                     [16]!)[E214F0]q [90E8]$[A5BAD1]L[E9C60BCD1A]^[86]BlO[E589F7A011]s[ACB4]A\[8816] J[00]b[05A6]X[CF96]^[C1]~`\[E4]A[F2C4BB81BBF496DC]~[CBEBD592]d[B1C8]
                                                                     @[C811D1]t
                                                                     [FE96B5]`[F0A616BE]ru
                                                                     [13]:[8B]\[D19C07]I[FD1E8E]%P:[B3]v[8D]S[E68405F3]vw4[01]f[DCBD95D8D3]^[FE]z[F9EAF5ECF5DB97AFDE]h-[85B9]Y$[B0BA]LP[908CB4]'[F5]}[8F],[FB]3K[ABD9AEECCF]W~[E8AA]r[C2]Z[E99EB9] p[B0B1E01D]
                                                                     [A9DB88]C[A7]E[02]|[CC]$"X[01]^[A1]/[B3]^[0192]L[AC] t[02969C]L[03D0]S[8500]7N[BA]Bs$[B1]+S[CE04BAA4E1]Sfa[EDB69C081B].[B9C40C]X[9399]rp[A0]j[F5]'u[AC0F],5[1ED68B]D[E4A69015DEB0BC]8[85]$[81]\[E8]H[E011]P[FE]$?3[B4]5[C919A9D7F3]S[CBF1]8I:q[D6]tX[D1F6B8]^[16]H[890B]][118CC2]n[B705BBA5CACA1CDD]9[D8A3]# [B0B4B2]H}[E6]2{-)[F2]&[C8BCD883ACA106881A]QT[1990]Ho[9AE9]C[AA]9b&[A4A1CAF7E4EA]L[F1]L[1AA4E8]TW[898A]RK[0401E9E0EFA7ECC0F7]&C[02]$[028E]r[BFACEC]D,[B2BFACAA9A]:[0296]3[ED8011]G[C302A01ABD]T[B9]D[98A2D8]RI[A58F]bJ[E5020CDA]f&[94DDE5B4D992EC]*[A7B5CDEA02DA]O[B6]yS@[90]5[9716][[16F6DEE1F7]n/"[F0]3[14]+[A7]}[0B]|[A1]g[F3B4]X[9AC0A680C9]o[D6898F]u[D8E891]:b[E59FBD0306]?[D5C8]'[D2EF]O&[A3E1F0]X[FC1305]-r<8[1D929B96E8190C]F[A3]S[D901]='[83F1]H[F50CFAA3D1A4]wz[AA]z[FABDC9E4]8[EB]:[190E]N[93]A[A3]a[AF]w[AABA]F[C7A793E1]I[BF9F]L[F5]&v[E7],[ECFC]z[F9DBEC]_[97AF]?[BE] 75[E013A913]e[E2]3[06]"[93EF]o[DFBC98BDFB]
                                                                     =[EEDE8FDCD4F7]m[06]V5=[E1180F]@b[BB9C]y[BAB0E0]V[DFE3]7[8CA8]8[B2]3UK[AC9C82000308]^!j[E9]M[F3]^q[17]r[82]C;[F2C3]@[0083]$[A9]0[C5]U[96]"[A18D]&[F0]Qh[00B0]d[B6A0]{[F78CCFAB]L[B6]SL[B9901E]x7[A9D5]1[E9FDF5]5D$t[17A4AD]|[89]
                                                                     &9[C2]~[C58F]h[DE81][[C18AA0]R[F1F9]^[8F90]/[DD96C3]rRV[82A8EF][[EC]'[8BCF]N[ACD7C512]3[CFF0]M[F6F1FDAB]g[BE1B]@F[E5F1F20408]U?[FCF21907E4]%[9109]b[9BB7E4]sNA][A9CA1E81]K[BC]cS[15CB]IQ[B9EF89B2]r[E0AB]0[1087]mk`[1AD4]3K[90]td[EF]J[C2A2]A9@[C8DD]9[FB]'6dq[1F]yx[E6]{xQ[08]L}[10A9]^[A3]Y[D0AEC2B8D4C1]mM-[15],[C1]{[7FB6A4]LHy^[08]#"[D0109B9C83F7]IR[00]b[1F1DE50BFF11]%[F67FB2]o>[F5]n[B28ABA]y[C8E8AD]*[C8]W[1488D084]a[B19780]Err?[E68D1C]O[E89F]z[BDE69414]][AE189A1292A0C89886]l[079CE7]/[D7BFBE]&[BA] [08CCA07FF3A97FB3F58106B2C9A302]T[EF]f[9A]v[CDA3]
                                                                     [04];gB[A581]
                                                                     P[03]%[D1E6]Q[93]tI#[1187]x[FF9FE4BD7FD39E]GM[0CF0F0]3%[0F96]@
                                                                     [BF]h[10]n[F5DDE5]s4[B38E]t[D6]u[ECCFB4]B[11E2C5C9871CD2EF85]*[C6]B&[81]][D3A4C7F4B9]H[F3B2]=^[BD]S[97DD]2\rX[D7]^[FA]MH[E2AE10E11112A288]CXxK[D3]3"[B682EAAD8DB3]4[0BEE].[1D9FCB]p[B6]F[94]V,[1B]ZL[9890]F[89]K[E9]D[E21290]?U;k[A4A6]4[C3]M[148A]O}[C3]U[9FEE80B3]E.[D1];[9E]Lz[83FEE0]d[1718FE]kR1[06]m[88]r[CC9B]}[FCA0ED8294]+[83]p[FD9D]0[E29C1D]A[AEC41FAD10EFAA]<B[96F0CB]-b[EE]c C\F[A904A6A1A9C3]K![1DF9DC]Q[A2D8]:[E0CC81]H[97]^Q[95]@Mu[131F09DF1E8008A8D9CD]X[E9FC1E]i*[D5]Q[9FF6]Wmf+[CE]V[C5]v6[B70FFA93088A1FD5]GQ[1281DAD4A3CE86DB86A4]7[0E8DA5]-[88BC]x8i[84CC]bj[1890A2]|[BC]lO&[83E3D3]Q[BB]/[A4]5[8BB911E8]ZV[A6A1]ZMO[D7]
                                                                     [Xl|[EFF9E015]t[AD]S[EA91]4][87D4B885C8A4C9E408E3]Rw[E9BB0C]%[9C]d[CC08F70E]6[CACFFC]`[93008AEB0404C8F1D3959FB09E8B7FA9ECA2F67F]][DBBA]A[01]M[0000
                                                                     --- localhost:55057->ssl.google-analytics.com:443 opened --
                                                                     --- ssl.google-analytics.com:443->localhost:55057 opened --
                                                                     ------ localhost:55057->ssl.google-analytics.com:443 ------
                                                                     GET /__utm.gif?utmwv=1.3&utmn=416519644&utmcs=utf-8&utmsr=1440x900&utmsc=32-bit&utmul=en&utmje=1&utmfl=10.0%20r32&utmdt=Gmail%3A%20Email%20from%20Google&utmhn=www.google.com&utmhid=745216&utmr=http://mail.google.com/&utmp=/mail/gaia/homepage&utmac=UA-992684-1&utmcc=__utma%3D173272373.191646898.1259953716.1260504333.1260508237.4%3B%2B__utmz%3D173272373.1259953716.1.1.utmccn%3D(direct)%7Cutmcsr%3D(direct)%7Cutmcmd%3D(none)%3B%2B HTTP/1.1
                                                                     User-Agent: Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.10
                                                                     Host: ssl.google-analytics.com
                                                                     Accept: text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1
                                                                     Accept-Language: en,ja;q=0.9,fr;q=0.8,de;q=0.7,es;q=0.6,it;q=0.5,pt;q=0.4,pt-PT;q=0.3,nl;q=0.2,sv;q=0.1,nb;q=0.1,da;q=0.1,fi;q=0.1,ru;q=0.1,pl;q=0.1,zh-CN;q=0.1,zh-TW;q=0.1,ko;q=0.1
                                                                     Accept-Charset: iso-8859-1, utf-8, utf-16, *;q=0.1
                                                                     Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0
                                                                     Referer: https://www.google.com/accounts/ServiceLogin?service=mail&passive=true&rm=false&continue=http%3A%2F%2Fmail.google.com%2Fmail%2F%3Fui%3Dhtml%26zy%3Dl&bsv=zpwhtygjntrz&scc=1&ltmpl=default&ltmplcache=2
                                                                     Connection: Keep-Alive, TE
                                                                     TE: deflate, gzip, chunked, identity, trailers


                                                                     ------ ssl.google-analytics.com:443->localhost:55057 ------
                                                                     HTTP/1.1 200 OK
                                                                     Date: Fri, 11 Dec 2009 05:10:39 GMT
                                                                     Content-Length: 35
                                                                     Pragma: no-cache
                                                                     Cache-Control: private, no-cache, no-cache=Set-Cookie, proxy-revalidate
                                                                     Expires: Wed, 19 Apr 2000 11:43:00 GMT
                                                                     Last-Modified: Wed, 21 Jan 2004 19:50:30 GMT
                                                                     Content-Type: image/gif
                                                                     Server: Golfe
                                                                     X-XSS-Protection: 0


                                                                     ------ ssl.google-analytics.com:443->localhost:55057 ------
                                                                     GIF89a[0100010080FF00FFFFFF000000],[0000000001000100000202]D[0100];
                                                                     --- localhost:55063->mail.google.com:443 opened --
                                                                     --- mail.google.com:443->localhost:55063 opened --
                                                                     ------ localhost:55063->mail.google.com:443 ------
                                                                     GET /mail/images/c.gif?t=1260508237309 HTTP/1.1
                                                                     User-Agent: Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.10
                                                                     Host: mail.google.com
                                                                     Accept: text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1
                                                                     Accept-Language: en,ja;q=0.9,fr;q=0.8,de;q=0.7,es;q=0.6,it;q=0.5,pt;q=0.4,pt-PT;q=0.3,nl;q=0.2,sv;q=0.1,nb;q=0.1,da;q=0.1,fi;q=0.1,ru;q=0.1,pl;q=0.1,zh-CN;q=0.1,zh-TW;q=0.1,ko;q=0.1
                                                                     Accept-Charset: iso-8859-1, utf-8, utf-16, *;q=0.1
                                                                     Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0
                                                                     Referer: https://www.google.com/accounts/ServiceLogin?service=mail&passive=true&rm=false&continue=http%3A%2F%2Fmail.google.com%2Fmail%2F%3Fui%3Dhtml%26zy%3Dl&bsv=zpwhtygjntrz&scc=1&ltmpl=default&ltmplcache=2
                                                                     Cookie: PREF=ID=291532419a6b3600:U=d01abcb07191b0e7:TM=1259800278:LM=1259800513:S=xFWh6CVa5Wo_vKuQ; GMAIL_RTT=67; NID=29=KmI8BpoSBchC0maHjFQhfWFJ8XttR0J0O0qCR88QegHWx4bixekFavQCadjGxe4bqNNJQPM54qsQWbZSjN0J1TTPWqs-EUUoMmf_gexTtcmLZq5WEZrI6XFoWKHpmRmZ; TZ=300
                                                                     Cookie2: $Version=1
                                                                     Connection: Keep-Alive, TE
                                                                     TE: deflate, gzip, chunked, identity, trailers


                                                                     ------ mail.google.com:443->localhost:55063 ------
                                                                     HTTP/1.1 200 OK
                                                                     Last-Modified: Fri, 02 Oct 2009 23:27:24 GMT
                                                                     Cache-control: max-age=31536000
                                                                     Expires: Sat, 11 Dec 2010 05:10:41 GMT
                                                                     Content-Type: image/gif
                                                                     Content-Length: 43
                                                                     Date: Fri, 11 Dec 2009 05:10:41 GMT
                                                                     X-Content-Type-Options: nosniff
                                                                     X-XSS-Protection: 0
                                                                     X-Frame-Options: SAMEORIGIN
                                                                     Server: GFE/2.0


                                                                     ------ mail.google.com:443->localhost:55063 ------
                                                                     GIF89a[0100010080FF00C0C0C0000000]![F9040100000000],[0000000001000100000202]D[0100];
                                                                     --- localhost:55068->blackboard.bowdoin.edu:443 opened --
                                                                     --- blackboard.bowdoin.edu:443->localhost:55068 opened --
                                                                     --- localhost:55068->blackboard.bowdoin.edu:443 closed --
                                                                     --- blackboard.bowdoin.edu:443->localhost:55068 closed --
                                                                     --- localhost:55072->blackboard.bowdoin.edu:443 opened --
                                                                     --- blackboard.bowdoin.edu:443->localhost:55072 opened --
                                                                     --- localhost:55072->blackboard.bowdoin.edu:443 closed --
                                                                     --- blackboard.bowdoin.edu:443->localhost:55072 closed --
                                                                     --- localhost:55076->blackboard.bowdoin.edu:443 opened --
                                                                     --- blackboard.bowdoin.edu:443->localhost:55076 opened --
                                                                     ------ localhost:55076->blackboard.bowdoin.edu:443 ------
                                                                     GET / HTTP/1.1
                                                                     User-Agent: Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.10
                                                                     Host: blackboard.bowdoin.edu
                                                                     Accept: text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1
                                                                     Accept-Language: en,ja;q=0.9,fr;q=0.8,de;q=0.7,es;q=0.6,it;q=0.5,pt;q=0.4,pt-PT;q=0.3,nl;q=0.2,sv;q=0.1,nb;q=0.1,da;q=0.1,fi;q=0.1,ru;q=0.1,pl;q=0.1,zh-CN;q=0.1,zh-TW;q=0.1,ko;q=0.1
                                                                     Accept-Charset: iso-8859-1, utf-8, utf-16, *;q=0.1
                                                                     Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0
                                                                     Cookie: __utmz=253559834.1260476440.5.6.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=bowdoin%20maintenance; __utma=253559834.1977986253.1259958233.1260363730.1260476440.5; __utmc=253559834
                                                                     Cookie2: $Version=1
                                                                     Connection: Keep-Alive, TE
                                                                     TE: deflate, gzip, chunked, identity, trailers


                                                                     ------ blackboard.bowdoin.edu:443->localhost:55076 ------
                                                                     HTTP/1.1 302 Moved Temporarily
                                                                     Date: Fri, 11 Dec 2009 05:11:01 GMT
                                                                     Server: Apache/1.3.37 (Unix) mod_ssl/2.8.28 OpenSSL/0.9.8d mod_jk/1.2.25
                                                                     Set-Cookie: JSESSIONID=2174762D4F1A50508FFAB4700C880D8B.root; Path=/; Secure
                                                                     X-Blackboard-product: Blackboard Academic Suite&#8482; 8.0.422.8
                                                                     Location: https://blackboard.bowdoin.edu/webapps/login?action=portal_direct_entry
                                                                     Content-Length: 0
                                                                     Keep-Alive: timeout=15, max=100
                                                                     Connection: Keep-Alive
                                                                     Content-Type: text/html;charset=UTF-8


                                                                     ------ localhost:55076->blackboard.bowdoin.edu:443 ------
                                                                     GET /webapps/login?action=portal_direct_entry HTTP/1.1
                                                                     User-Agent: Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.10
                                                                     Host: blackboard.bowdoin.edu
                                                                     Accept: text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1
                                                                     Accept-Language: en,ja;q=0.9,fr;q=0.8,de;q=0.7,es;q=0.6,it;q=0.5,pt;q=0.4,pt-PT;q=0.3,nl;q=0.2,sv;q=0.1,nb;q=0.1,da;q=0.1,fi;q=0.1,ru;q=0.1,pl;q=0.1,zh-CN;q=0.1,zh-TW;q=0.1,ko;q=0.1
                                                                     Accept-Charset: iso-8859-1, utf-8, utf-16, *;q=0.1
                                                                     Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0
                                                                     Cookie: JSESSIONID=2174762D4F1A50508FFAB4700C880D8B.root; __utmz=253559834.1260476440.5.6.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=bowdoin%20maintenance; __utma=253559834.1977986253.1259958233.1260363730.1260476440.5; __utmc=253559834
                                                                     Cookie2: $Version=1
                                                                     Connection: Keep-Alive, TE
                                                                     TE: deflate, gzip, chunked, identity, trailers


                                                                     ------ blackboard.bowdoin.edu:443->localhost:55076 ------
                                                                     HTTP/1.1 302 Moved Temporarily
                                                                     Date: Fri, 11 Dec 2009 05:11:01 GMT
                                                                     Server: Apache/1.3.37 (Unix) mod_ssl/2.8.28 OpenSSL/0.9.8d mod_jk/1.2.25
                                                                     Location: https://blackboard.bowdoin.edu/webapps/login/?action=portal_direct_entry
                                                                     Keep-Alive: timeout=15, max=99
                                                                     Connection: Keep-Alive
                                                                     Transfer-Encoding: chunked
                                                                     Content-Type: text/plain


                                                                     ------ blackboard.bowdoin.edu:443->localhost:55076 ------
                                                                     0


                                                                     --- localhost:55080->blackboard.bowdoin.edu:443 opened --
                                                                     --- blackboard.bowdoin.edu:443->localhost:55080 opened --
                                                                     ------ localhost:55080->blackboard.bowdoin.edu:443 ------
                                                                     GET /webapps/login/?action=portal_direct_entry HTTP/1.1
                                                                     User-Agent: Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.10
                                                                     Host: blackboard.bowdoin.edu
                                                                     Accept: text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1
                                                                     Accept-Language: en,ja;q=0.9,fr;q=0.8,de;q=0.7,es;q=0.6,it;q=0.5,pt;q=0.4,pt-PT;q=0.3,nl;q=0.2,sv;q=0.1,nb;q=0.1,da;q=0.1,fi;q=0.1,ru;q=0.1,pl;q=0.1,zh-CN;q=0.1,zh-TW;q=0.1,ko;q=0.1
                                                                     Accept-Charset: iso-8859-1, utf-8, utf-16, *;q=0.1
                                                                     Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0
                                                                     Cookie: JSESSIONID=2174762D4F1A50508FFAB4700C880D8B.root; __utmz=253559834.1260476440.5.6.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=bowdoin%20maintenance; __utma=253559834.1977986253.1259958233.1260363730.1260476440.5; __utmc=253559834
                                                                     Cookie2: $Version=1
                                                                     Connection: Keep-Alive, TE
                                                                     TE: deflate, gzip, chunked, identity, trailers


                                                                     ------ blackboard.bowdoin.edu:443->localhost:55080 ------
                                                                     HTTP/1.1 302 Moved Temporarily
                                                                     Date: Fri, 11 Dec 2009 05:11:01 GMT
                                                                     Server: Apache/1.3.37 (Unix) mod_ssl/2.8.28 OpenSSL/0.9.8d mod_jk/1.2.25
                                                                     Set-Cookie: JSESSIONID=58FCAF958951FF3E3D17095DBF17A431.root; Path=/webapps/login; Secure
                                                                     X-Blackboard-product: Blackboard Academic Suite&#8482; 8.0.422.8
                                                                     Pragma: no-cache
                                                                     Cache-Control: no-cache
                                                                     Set-Cookie: session_id=8A74E93DE6EA864D7AB02E681F5C763B; Path=/
                                                                     Location: https://blackboard.bowdoin.edu/webapps/portal/frameset.jsp
                                                                     Content-Length: 0
                                                                     Keep-Alive: timeout=15, max=100
                                                                     Connection: Keep-Alive
                                                                     Content-Type: application/octet-stream;charset=UTF-8


                                                                     ------ localhost:55076->blackboard.bowdoin.edu:443 ------
                                                                     GET /webapps/portal/frameset.jsp HTTP/1.1
                                                                     User-Agent: Opera/9.80 (Macintosh; Intel Mac OS X; U; en) Presto/2.2.15 Version/10.10
                                                                     Host: blackboard.bowdoin.edu
                                                                     Accept: text/html, application/xml;q=0.9, application/xhtml+xml, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1
                                                                     Accept-Language: en,ja;q=0.9,fr;q=0.8,de;q=0.7,es;q=0.6,it;q=0.5,pt;q=0.4,pt-PT;q=0.3,nl;q=0.2,sv;q=0.1,nb;q=0.1,da;q=0.1,fi;q=0.1,ru;q=0.1,pl;q=0.1,zh-CN;q=0.1,zh-TW;q=0.1,ko;q=0.1
                                                                     Accept-Charset: iso-8859-1, utf-8, utf-16, *;q=0.1
                                                                     Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0
                                                                     Cookie: JSESSIONID=2174762D4F1A50508FFAB4700C880D8B.root; session_id=8A74E93DE6EA864D7AB02E681F5C763B; __utmz=253559834.1260476440.5.6.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=bowdoin%20maintenance; __utma=253559834.1977986253.1259958233.1260363730.1260476440.5; __utmc=253559834
                                                                     Cookie2: $Version=1
                                                                     Connection: Keep-Alive, TE
                                                                     TE: deflate, gzip, chunked, identity, trailers


                                                                     ------ blackboard.bowdoin.edu:443->localhost:55076 ------
                                                                     HTTP/1.1 302 Moved Temporarily
                                                                     Date: Fri, 11 Dec 2009 05:11:01 GMT
                                                                     Server: Apache/1.3.37 (Unix) mod_ssl/2.8.28 OpenSSL/0.9.8d mod_jk/1.2.25
                                                                     Set-Cookie: JSESSIONID=DDEE3B86573DA539964C22D0A84F2CFE.root; Path=/webapps/portal; Secure
                                                                     X-Blackboard-product: Blackboard Academic Suite&#8482; 8.0.422.8
                                                                     Set-Cookie: session_id=8A74E93DE6EA864D7AB02E681F5C763B; Path=/
                                                                     Location: http://blackboard.bowdoin.edu/webapps/portal/frameset.jsp
                                                                     Content-Length: 0
                                                                     Keep-Alive: timeout=15, max=98
                                                                     Connection: Keep-Alive
                                                                     Content-Type: text/html;charset=UTF-8


                                                                     --- blackboard.bowdoin.edu:443->localhost:55076 closed --
                                                                     --- localhost:55076->blackboard.bowdoin.edu:443 closed --
                                                                     --- blackboard.bowdoin.edu:443->localhost:55080 closed --
                                                                     --- localhost:55080->blackboard.bowdoin.edu:443 closed --
----------------------------------------------------------------------------------------------------------------------------------------------------------------
    
Short Answers:
    1. Firefox 3 gives the user a stern warning before allowing them to browse a site with self-signed certificates. Such a strong warning, in fact, that most users will not click through it.
    2. The advantage of Firefox's approach is that it very much curbs non-technical users from browsing sites with self-signed certificates.  While this would normally be a good thing, the disadvantage is that in reality nearly all self-signed certificates are benign.
    3. I like the approaches taken by various browsers already out there.  For example, Chrome displays the "https" in red text with a strikethrough when the certificate is self-signed.  Likewise, Opera displays a green question mark in the top-right corner, but this isn't immediately obvious.  In order to solve the problem, perhaps a cheaper way of validating certificates would be helpful, as site owners might actually validate against Verisign or somewhere else along the chain.
    
As far as group participation goes, we wrote the project using pair programming techniques, and as such all contributed to the creation.  We were all in the same room, brainstorming and took turns writing - no one person is responsible for any particular part.