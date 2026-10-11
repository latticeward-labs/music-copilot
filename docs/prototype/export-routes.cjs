#!/usr/bin/env node
"use strict";
/* Materialize repository-native routes.json from the exact interactive
   routes.js source; avoid a second independently edited route graph. */
const fs=require("node:fs"),path=require("node:path");
const data=require("./routes.js");
const output=path.join(__dirname,"routes.json");
if(!data || !Array.isArray(data.nodes) || !data.scenarios || !data.recoverySafety) throw Error("Invalid route manifest");
fs.writeFileSync(output,JSON.stringify(data,null,2)+"\n");
console.log("Exported",output,":",data.nodes.length,"nodes and",Object.keys(data.scenarios).length,"GS");
