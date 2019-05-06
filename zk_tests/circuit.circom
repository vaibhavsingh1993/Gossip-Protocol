include "../../circomlib/circuits/bitify.circom";
include "../../circomlib/circuits/eddsa.circom";

template Test(n) {
  signal input M;
  signal input A;
  signal input R;
  signal input S;

  var i;
  component eddsaVer = EdDSAVerifier(n);
  component bM = Num2Bits(n);
  component bA = Num2Bits(256);
  component bR = Num2Bits(256);
  component bS = Num2Bits(256);

  bM.in <== M;
  bA.in <== A;
  bR.in <== R;
  bS.in <== S;

  for (i=0; i<n; i++) {
    eddsaVer.msg[i] <== bM.out[i];
  }

  for (i=0; i<256; i++) {
    eddsaVer.A[i]  <== bA.out[i];
    eddsaVer.R8[i] <== bR.out[i];
    eddsaVer.S[i]  <== bS.out[i];
  }

}

component main = Test(84);
