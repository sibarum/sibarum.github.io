Imports System
Imports System.Collections.Generic
Imports System.IO
Imports System.Linq
Imports System.Text
Imports System.Text.RegularExpressions

Module Driver
NANOBUF

    Function Hex(h As String) As String
        Return Encoding.UTF8.GetString(Enumerable.Range(0, h.Length \ 2).Select(Function(i) Convert.ToByte(h.Substring(2 * i, 2), 16)).ToArray())
    End Function

    Function Main(a As String()) As Integer
        If a(0) = "r" Then
            Dim rows As List(Of String())
            Try
                rows = nbr(a(1), a(2), a(3))
            Catch
                Return 1
            End Try
            If rows Is Nothing Then Return 1
            File.WriteAllText(a(4), String.Concat(rows.Select(Function(r) String.Concat(r.Select(Function(f) "[" & f & "]")) & vbLf)))
            Return 0
        End If
        For Each line In File.ReadAllText(a(1)).Split(ChrW(10))
            If line = "" Then Continue For
            Try
                nbw(a(2), line.Split(","c).Select(AddressOf Hex).ToArray())
            Catch
                Return 1
            End Try
        Next
        Return 0
    End Function
End Module
