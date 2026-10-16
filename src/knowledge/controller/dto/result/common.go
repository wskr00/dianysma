package result

type DeleteResult struct {
	ID      string `json:"id" jsonschema:"UUID do registro solicitado para exclusão." format:"uuid"`
	Deleted bool   `json:"deleted" jsonschema:"true quando o registro foi excluído; false quando o ID não existia."`
}
