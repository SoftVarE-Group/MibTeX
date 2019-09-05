# MibTeX
Minimalistic tool to manage your references with BibTeX

### Setup Instructions

MibTeX is an eclipse project that generates an html page for browsing your literature specified in BibTeX's format.
Run the main method in class `BibtexViewer` with the following arguments to obtain your page (example arguments are given in brackets):

0. Directory of your BibTeX repository ("D:/Literatur/BibTags/")
1. The output directory ("D:/Literatur/")
2. Relative path inside the output directory where the page should be generated ("")
3. Relative directory to pdfs ("PDF/")?
4. Relative path inside the output directory where your pdfs are stored ("PDF/")
5. List of tags ("tt-tags")
6. Target format: If you just want to generate the html page use "HTML_NEW".
7. "true" if the output directory should be cleaned before build, "false" else