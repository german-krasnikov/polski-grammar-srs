// Generated from dsl/exam-assets.data-contract, dsl/exam-assets/exam-audio.core-data
// source-sha256=c6b7533fc07e72e93d4c774104e11582136216db55478f5f7cc0bd7189804887
// Edit the source, then run biome generate.

export interface ExamAudioAssetRow {
  readonly examId: string;
  readonly officialDownloadUrl: string;
  readonly sha256: string;
  readonly webPath: string;
  readonly localSourcePath: string;
  readonly webSha256?: string;
  readonly webLocalSourcePath?: string;
}

export const EXAM_AUDIO_ASSET_ROW_ROWS: readonly ExamAudioAssetRow[] = [
  {
    examId: "certyfikat-polski-B1-2017",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2017/03/Egzamin-Przyk%C5%82adowy-B1-gru2016.mp3",
    sha256: "e7d78c0f1bf64ef6ebf83e7d8b9d38f30539a2d33f205d40d230d56d03fb2747",
    webPath: "exam-audio/B1-2017.mp3",
    localSourcePath: "ExamBin/pl/B1/2017/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B1-2019",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2019/09/B1_audio.mp3",
    sha256: "55a563e5a53cb6f576e565fd4656fd705074cf25073a75804afeb19e3159ede1",
    webPath: "exam-audio/B1-2019.mp3",
    localSourcePath: "ExamBin/pl/B1/2019/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B1-2020",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2020/12/B1_2020_03.mp3",
    sha256: "b88f61dea88036425bfd6990c07f308264a42b06d0aa9ce75a64a82bf3e840c8",
    webPath: "exam-audio/B1-2020.mp3",
    localSourcePath: "ExamBin/pl/B1/2020/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B2-2017",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2017/03/Egzamin-przyk%C5%82adowy-B2-listopad-2016-poprawiony.mp3",
    sha256: "d1d865a43dd4a6e91ef2508e1476cdffca2413e0abcd25a9371b568b702cf84e",
    webPath: "exam-audio/B2-2017.mp3",
    localSourcePath: "ExamBin/pl/B2/2017/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B2-2019",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2019/09/B2_audio.mp3",
    sha256: "4aa580740d38b5f5a3cbcd10c0b6b4e9ed700d6c1407dce60c2733445d9b9650",
    webPath: "exam-audio/B2-2019.mp3",
    localSourcePath: "ExamBin/pl/B2/2019/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B2-2020",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2020/12/B2_03_2020.mp3",
    sha256: "e931d9c4466ee5186a6b47fb3a01201a93882791c8bbc74a2ea8275ba9f3d72e",
    webPath: "exam-audio/B2-2020.mp3",
    localSourcePath: "ExamBin/pl/B2/2020/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B2-2022-02",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2026/01/2022.02.6-7_B2_Plik_dzwiekowy.mp3",
    sha256: "f7d5ccc4f4bc023b07125d515b2d0a2ebe6bf502b573c1c91994b556b042e8ad",
    webPath: "exam-audio/B2-2022-02.mp3",
    localSourcePath: "ExamBin/pl/B2/2022-02/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B2-2023-02",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/07/2023_02_5_6_B2_plik_dzwiekowy.mp3.mp3",
    sha256: "ff699635fba80bcaeb1b2febb6d98a70e245d17d8ab0cb4fbe2ad25e86fe6049",
    webPath: "exam-audio/B2-2023-02.mp3",
    localSourcePath: "ExamBin/pl/B2/2023-02/audio.mp3",
    webSha256: "717aa32c86340c7b5b193aa6a4befe6e790392f23ffefe61a16d9474e8db2a8c",
    webLocalSourcePath: "ExamBin/web/B2/2023-02/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B2-2023-06",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/07/2023_06_24_25_B2_plik_dzwiekowy.mp3",
    sha256: "bd5dfe1c3ebfe65a77974df59ac019f14919aad201f7daa3d1a364d51198791a",
    webPath: "exam-audio/B2-2023-06.mp3",
    localSourcePath: "ExamBin/pl/B2/2023-06/audio.mp3",
    webSha256: "b3bd3e0882a2027c43ce6fee964a473982ebf2b05860ca5da3c1c84c3b796d68",
    webLocalSourcePath: "ExamBin/web/B2/2023-06/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B2-2024-02",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/02/4-5.02.2024-B2_plik_dzwiekowy.mp3",
    sha256: "25553efda608359ba51883e8afdda4fa8b5ef0dc81ad4f37f835cae2b858ae70",
    webPath: "exam-audio/B2-2024-02.mp3",
    localSourcePath: "ExamBin/pl/B2/2024-02/audio.mp3",
    webSha256: "3453c7158733f5b3ac3680b7c443f1cf4c9cb2036f634c61b65b0de16160758f",
    webLocalSourcePath: "ExamBin/web/B2/2024-02/audio.mp3",
  },
  {
    examId: "certyfikat-polski-B2-2024-06",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/02/22-23.06.2024-B2_plik_dzwiekowy.mp3",
    sha256: "c09eb89ec3a8db76d0c4e96418d164c74aa376aa7415a7749e534023bb546a32",
    webPath: "exam-audio/B2-2024-06.mp3",
    localSourcePath: "ExamBin/pl/B2/2024-06/audio.mp3",
    webSha256: "f8b17f45b64ec6c7fb5e70657b5bf4166d21505479c405d149ec2d858e14e6bc",
    webLocalSourcePath: "ExamBin/web/B2/2024-06/audio.mp3",
  },
  {
    examId: "certyfikat-polski-C1-2022-03",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2026/02/2022.03.26-27_C1_Plik_dzwiekowy_v2.mp3",
    sha256: "119de10db6f3d1aea9970b5f1790d38ffa2a912682ccfdb4816f98c91d287bca",
    webPath: "exam-audio/C1-2022-03.mp3",
    localSourcePath: "ExamBin/pl/C1/2022-03/audio.mp3",
    webSha256: "06566b785cf8712fd3195099aa1eb4c6c9ea140e4773dbe3c893722118cce095",
    webLocalSourcePath: "ExamBin/web/C1/2022-03/audio.mp3",
  },
  {
    examId: "certyfikat-polski-C1-2022-11",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2026/01/2022.11.5-6_C1_Plik_dzwiekowy.mp3",
    sha256: "53448f2c113e2be985ad65ec1ed7fd3362067e038f76431badf30b8438689ab5",
    webPath: "exam-audio/C1-2022-11.mp3",
    localSourcePath: "ExamBin/pl/C1/2022-11/audio.mp3",
    webSha256: "b1fb9447413b42049b837d5de22990555264c2a02b538791c3b5682350645d26",
    webLocalSourcePath: "ExamBin/web/C1/2022-11/audio.mp3",
  },
  {
    examId: "certyfikat-polski-C1-2023-04",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/07/2023_04_15_16_C1_plik_dzwiekowy.mp3",
    sha256: "7836d6d038029c2eebfa4491b9bcd3b1dc7fea83784189dc88c33196072afd28",
    webPath: "exam-audio/C1-2023-04.mp3",
    localSourcePath: "ExamBin/pl/C1/2023-04/audio.mp3",
  },
  {
    examId: "certyfikat-polski-C1-2023-11",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/07/2023_11_18_19_C1_plik_dzwiekowy.mp3",
    sha256: "920624ab06c0585ec5a584523e0ec5ae7eba524ded1561583f2363c2c3a566ef",
    webPath: "exam-audio/C1-2023-11.mp3",
    localSourcePath: "ExamBin/pl/C1/2023-11/audio.mp3",
    webSha256: "0df5c919fc1bf9aad892b127166edf32fff63af5953070214b730815e8ebea1e",
    webLocalSourcePath: "ExamBin/web/C1/2023-11/audio.mp3",
  },
  {
    examId: "certyfikat-polski-C1-2024-04",
    officialDownloadUrl: "https://certyfikatpolski.pl/wp-content/uploads/2025/02/20-21.04.2024-C1_plik_dzwiekowy.mp3",
    sha256: "4741dbbdb11ee04d0ad778d6c73d6314c345e97a737337bbd5b353c504288cbf",
    webPath: "exam-audio/C1-2024-04.mp3",
    localSourcePath: "ExamBin/pl/C1/2024-04/audio.mp3",
    webSha256: "2f03d9275e821afd44be024b4fb60bb5e90d76f56c41ea28ac684b716f23e52f",
    webLocalSourcePath: "ExamBin/web/C1/2024-04/audio.mp3",
  },
];
