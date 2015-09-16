#!/usr/bin/lua

function rline(line)
   return line:match('([^,]+),([^,]+),"?([^,"]+)"?,"?([^"\r\n]+)')
end

function rfill(s, l)
   local f = math.max(0, l - s:len())
   return ' ' .. s .. (' '):rep(f + 1)
end

function pline(lat, lon, name, desc)
   io.write('|', rfill(lat, 9), '|', rfill(lon, 11), '|', rfill(name, name_max), '|', rfill(desc, desc_max), '|\n')
end

function dash(n)
   return ('-'):rep(n)
end

name_max, desc_max = 0, 0
header = true

f = io.input()

for line in f:lines() do
   local lat, lon, name, desc = rline(line)
   if desc then
      name_max = math.max(name_max, name:len())
      desc_max = math.max(desc_max, desc:len())
   end
end

f:seek("set", 0)

for line in f:lines() do
   print(line)
   local lat, lon, name, desc = rline(line)
   if desc then
      pline(lat, lon, name, desc)
      if header then
	 pline(dash(9), dash(11), dash(name_max), dash(desc_max))
	 header = false
      end
   end
end
