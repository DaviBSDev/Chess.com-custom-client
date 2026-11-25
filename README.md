# ChessSkin — WebView do chess.com com peças próprias (Vexel)

    vexel build

Suas peças: coloque as imagens na pasta  Imagens/ChessSkin  (/sdcard/Pictures/ChessSkin/).
O app cria a pasta na primeira abertura e mostra o caminho.

Nomes (minúsculas; .png, .webp, .jpg ou .jpeg):
  brancas: wp wn wb wr wq wk     pretas: bp bn bb br bq bk
  (p=peão n=cavalo b=bispo r=torre q=dama k=rei)

Arquivo ausente ou inválido = o site usa a peça padrão dele.
Para testar: copie o conteúdo de pecas_teste/ para a pasta acima.
Cada imagem é lida uma vez e fica em cache (recarrega se o arquivo mudar).
Sem permissão de imagens, o app funciona normal, só com as peças padrão.
Se as peças não mudarem, ajuste `pieceRe` em MainActivity.kt (padrão da URL).

Clique direito: segure um dedo e use o OUTRO dedo (toque/arraste) = botão direito.
Serve para desenhar setas e marcar casas no tabuleiro.

Ícone: troque `src/res/mipmap-xxxhdpi/ic_launcher.png` (PNG 192x192) pelo que quiser.
Nome do app: `android:label` no AndroidManifest.xml e `name` no vexel.build.
# Chess.com-custom-client
